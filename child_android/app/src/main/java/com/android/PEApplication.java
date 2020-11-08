package com.android;

import android.app.Activity;
import android.content.Context;
import android.util.Log;


import androidx.multidex.BuildConfig;
import androidx.multidex.MultiDex;

import com.alibaba.fastjson.JSON;
import com.android.bean.Messages;
import com.android.bean.User;
import com.android.core.AppService;
import com.android.signal.RTCSignalClient;
import com.android.utils.OkHttpTool;
import com.android.utils.SPUtils;
import com.android.utils.ALog;
import com.android.utils.AppUtil;
import com.android.utils.DateUtil;
import com.android.utils.ImageUtil;
import com.android.core.Constants;
import com.android.core.HttpResult;
import com.android.core.HttpUtil;
import com.android.model.Message;
import com.android.model.MessageEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;

import org.apache.commons.lang.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.java_websocket.WebSocket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.common.Callback;
import org.xutils.db.table.TableEntity;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;


/**
 * 全局变量
 */
public class PEApplication extends SPUtils {
    public static PEApplication INSTANCE;
    public  RTCSignalClient mSignalClient = null;//信号客户端
    private JSONObject gUser;
    private User user;
    public static DbManager.DaoConfig daoConfig;
    private Gson gson=new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        ALog.LOG = true;
        MultiDex.install(this);
        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG);
        // 全局默认信任所有https域名 或 仅添加信任的https域名
        // 使用RequestParams#setHostnameVerifier(...)方法可设置单次请求的域名校验
        x.Ext.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        Config.DEBUG = true;
        PlatformConfig.setWeixin(Constants.WECHAT_APPID, Constants.WECHAT_APPSECRET);
        UMShareAPI.get(this);

        /**
         * 初始化DaoConfig配置
         */
        daoConfig = new DbManager.DaoConfig()
                //设置数据库名，默认xutils.db
                .setDbName(Constants.DB_NAME)
                //设置数据库路径，默认存储在app的私有目录
                .setDbDir(ImageUtil.getDir(ImageUtil.DB_DIR))
                //设置数据库的版本号
                .setDbVersion(Constants.DB_VERSION)
                //设置数据库打开的监听
                .setDbOpenListener(new DbManager.DbOpenListener() {
                    @Override
                    public void onDbOpened(DbManager db) {
                        //开启数据库支持多线程操作，提升性能，对写入加速提升巨大
                        db.getDatabase().enableWriteAheadLogging();

                        ALog.e("onDbOpened.");
                    }
                })
                //设置数据库更新的监听
                .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                    @Override
                    public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
                        ALog.e("onUpgrade oldVersion：" + oldVersion + ", newVersion: " + newVersion);
                    }
                })
                //设置表创建的监听
                .setTableCreateListener(new DbManager.TableCreateListener() {
                    @Override
                    public void onTableCreated(DbManager db, TableEntity<?> table) {
                        ALog.e("onTableCreated：" + table.getName());
                    }
                });
        //设置是否允许事务，默认true
        //.setAllowTransaction(true)
        initAppStatusListener();
        connectStompClient();
    }

    /**
     * 初始化App
     */
    private void initAppStatusListener() {
        ForegroundCallbacks.init(this).addListener(new ForegroundCallbacks.Listener() {
            @Override
            public void onBecameForeground() {
                ALog.e("应用回到前台调用重连方法");
                connectStompClient();
            }

            @Override
            public void onBecameBackground() {
                ALog.e("应用退回到后台了");
            }
        });
    }
    private Activity mainActivity;
    private Activity languageActivity;
    public void setMainActivity(Activity activity){
        this.mainActivity=activity;
    }
    public void setLanguageActivity(Activity activity){
        this.languageActivity=activity;
    }
    public void closeActivity(){
        if (mainActivity!=null){
            mainActivity.finish();
        }
        if (languageActivity!=null){
            languageActivity.finish();
        }
    }
    private StompClient mStompClient;

    /**
     * 重连Stomp客户端
     */
    public void connectStompClient() {
        if (!AppUtil.isNetConnect()) {
            return;
        }
        if (!isLogin()) {
            return;
        }
        if (mStompClient == null) {
            initStompClient();//重新初始化
        }

    }

    /**
     * 获取Stomp客户端
     * @return
     */
    public StompClient getStompClient() {
        connectStompClient();
        return mStompClient;
    }
    /**
     * 初始化（连接）Stomp客户端
     */
    private void initStompClient() {
        mSignalClient = new RTCSignalClient(); // 创建信令服务器（音视频通话）
        mSignalClient.connect();
        String url = String.format(Constants.WS_URL, PEApplication.INSTANCE.getUserToken());
        mStompClient = Stomp.over(WebSocket.class, url);
        mStompClient.connect(true);
        ALog.e("开始连接---->" + url);
        mStompClient.lifecycle().subscribe(new Action1<LifecycleEvent>() {
            @Override
            public void call(LifecycleEvent lifecycleEvent) {
                switch (lifecycleEvent.getType()) {
                    case OPENED:
                        ALog.e("连接已开启 Stomp connection opened");
                        registerStompTopic();
                        break;
                    case ERROR:
                        mStompClient = null;
                        ALog.e("连接出错 Stomp Error:" + lifecycleEvent.getException());
                        break;
                    case CLOSED:
                        if (getStompClient() != null) {
                            getStompClient().disconnect();
                            mStompClient = null;
                        }
                        ALog.e("连接关闭 Stomp connection closed");
                        break;
                }
            }
        });
    }

    /**
     * 注册订阅事件
     */
    private void registerStompTopic() {
        if ( getStompClient()!=null){
            //普通消息订阅
            final Message msg = new Message();
            final String message = String.format("/user/%s/message", getUserLogin());
            ALog.e("普通消息订阅: "+ message);
            getStompClient().topic(message).subscribe(new Action1<StompMessage>() {
                @Override
                public void call(StompMessage stompMessage) {
                    Messages msg1 = JSON.parseObject(stompMessage.getPayload(), Messages.class);//解析后台数据
                    getAck(msg1.getId());//消息应答
                    msg.setId(0);
                    msg.setRead(false);//未读
                    msg.setSendSuccess(true);
                    msg.setSendUserLogin(msg1.getSendUserLogin());
                    msg.setAcceptUserLogin(msg1.getAcceptUserLogin());
                    msg.setContent(msg1.getContent());
                    msg.setSendState(msg1.getSubscribeValue());
                    msg.setType(msg1.getType());
                    msg.setSendTime(msg1.getSendTime());
                    msg.save();
                    ALog.e("接受到消息: " + msg.toString());
                    EventBus.getDefault().post(new MessageEvent(Constants.EVENT_MSG_TYPE_CHAT_RECEIVED_MSG, msg));
                }
            });
            //强制登出订阅
            String logout = String.format("/user/%s/logout", getUserLogin());
            ALog.e("强制登出订阅: "+ logout);
            getStompClient().topic(logout).subscribe(new Action1<StompMessage>() {
                @Override
                public void call(StompMessage stompMessage) {
                    Messages msg = JSON.parseObject(stompMessage.getPayload(), Messages.class);//解析后台数据
                    getAck(msg.getId());//消息应答
                    ALog.e("接受到强制登出消息: " + msg.toString());
                    EventBus.getDefault().post(new MessageEvent(Constants.EVENT_MSG_TYPE_LOGOUT, msg));
                }
            });
            //好友操作订阅
            String friendOptions = String.format("/user/%s/friendOptions", getUserLogin());
            ALog.e("好友操作订阅: "+ friendOptions);
            getStompClient().topic(friendOptions).subscribe(new Action1<StompMessage>() {
                @Override
                public void call(StompMessage stompMessage) {
                    Messages msg1 = JSON.parseObject(stompMessage.getPayload(), Messages.class);//解析后台数据
                    getAck(msg1.getId());//消息应答
                    ALog.e("接受到好友操作消息: " + msg1.toString());
                    if (msg1.getSubscribeValue()==1){//删除好友
                        EventBus.getDefault().post(new MessageEvent(Constants.EVENT_MSG_TYPE_CONTACT_REFRESH_SERVER, "刷新服务器通讯录"));
                    }
                    if (msg1.getSubscribeValue()==2 ){//同意好友
                        msg.setId(0);
                        msg.setRead(false);//未读
                        msg.setSendSuccess(true);
                        msg.setSendUserLogin(msg1.getSendUserLogin());
                        msg.setAcceptUserLogin(msg1.getAcceptUserLogin());
                        msg.setContent("我已同意加为好友，我们可以聊天啦~");
                        msg.setSendState(msg1.getSubscribeValue());
                        msg.setType(msg1.getType());
                        msg.setSendTime(msg1.getSendTime());
                        msg.save();
                        EventBus.getDefault().post(new MessageEvent(Constants.EVENT_MSG_TYPE_CONTACT_REFRESH_SERVER, "刷新服务器通讯录"));
                        EventBus.getDefault().post(new MessageEvent(Constants.EVENT_MSG_TYPE_CHAT_RECEIVED_MSG, msg));
                    }


                }
            });
            //已读状态通知订阅
            String readStatus = String.format("/user/%s/readStatus", getUserLogin());
            ALog.e("已读状态通知订阅: "+ readStatus);
            getStompClient().topic(readStatus).subscribe(new Action1<StompMessage>() {
                @Override
                public void call(StompMessage stompMessage) {
                    Messages msg = JSON.parseObject(stompMessage.getPayload(), Messages.class);//解析后台数据
                    getAck(msg.getId());//消息应答
                    ALog.e("接受到已读状态通知订阅: " + msg.toString());
                    if (msg.getSubscribeValue()==1){//已读
                        EventBus.getDefault().post(new MessageEvent(Constants.EVENT_MSG_TYPE_CHAT_READ, msg));
                    }
                }
            });
        }
    }

    /**
     * 获取用户信息
     * @return
     */
    public JSONObject getUserInfo() {
        if (gUser == null) {
            gUser = AppUtil.toJsonObject(getSp(Constants.SP_LAST_LOGIN_USER_INFO, ""));
        }
        return gUser;
    }
    public User getUser() {
        if (user == null) {
            Log.i("ss",getSp(Constants.SP_LAST_LOGIN_USER_INFO,""));
            user = gson.fromJson(getSp(Constants.SP_LAST_LOGIN_USER_INFO, ""),User.class);
        }
        return user;
    }

    //用户登录
    public String getUserLogin() {
        String login = getUserInfo().optString("login");
        if (StringUtils.isBlank(login)) {
            login = AppUtil.toJsonObject(getSp(Constants.SP_LAST_LOGIN_USER_LOGIN, "")).optString("login");
        }
        return login;
    }

    /**
     * 获取用户ID
     * @return
     */
    public String getUserInfoId() {
        String userInfoId = "";
        if (AppUtil.isNull(getUserInfo())) {
            JSONObject login = AppUtil.toJsonObject(getSp(Constants.SP_LAST_LOGIN_USER_LOGIN, ""));
            userInfoId = login.optString("userInfoId");
        } else {
            userInfoId = getUserInfo().optString("id");
        }
        ALog.e("current user info id:" + userInfoId);
        return userInfoId;
    }

    /**
     * 获取用户登录ID
     * @return
     */
    public String getUserLoginId() {
        JSONObject login = AppUtil.toJsonObject(getSp(Constants.SP_LAST_LOGIN_USER_LOGIN, ""));
        return login.optString("userLoginId");
    }

    public static boolean isEn() {
        return false;
    }

    /**
     * 是否已登录
     * @return
     */
    public boolean isLogin() {
        return StringUtils.isNotBlank(getUserLoginId());
    }

    /**
     * 获取用户令牌
     * @return
     */
    public String getUserToken() {
        return getSp(Constants.SP_LOGIN_USER_TOKEN, "");
    }

    /**
     * 用户登录成功
     * @param userInfo
     */
    public void userLoginCallback(final JSONObject userInfo) {
        setSp(Constants.SP_LOGIN_USER_TOKEN, userInfo.optString("idToken"));
        setSp(Constants.SP_LAST_LOGIN_USER_LOGIN, userInfo.toString());
        tryGetUserInfo(getUserLogin());
        initStompClient();
    }

    /**
     * 通过手机号查询用户信息
     * @param mobile
     */
    public void tryGetUserInfo(String mobile) {
        final RequestParams params = HttpUtil.requestParams(String.format("users/%s/profile", mobile));
        x.http().get(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                HttpResult httpResult = HttpResult.createWith(jsonObject);
                if (httpResult.isSuccess()) {
                    Log.i("data",httpResult.payload.toString());
                    setUserInfo(httpResult.payload.toString());

                } else {
                    ALog.e(httpResult.code + ":" + httpResult.returnMsg);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                HttpUtil.onError(params.getUri(), ex);
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    /**
     * 消息应答
     * @param msgId
     */
    public void getAck(String msgId) {
        final RequestParams params = HttpUtil.requestParams("messages/ack");
        Map<String,Object> map=new HashMap<>();
        map.put("msgId",msgId);
        OkHttpTool.httpGet(params.toString(), map, new OkHttpTool.ResponseCallback() {
            @Override
            public void onResponse(boolean isSuccess, int responseCode, String response, Exception exception) {
            }
        });
    }
    /**
     * 退出登录关闭资源
     */
    public void userLogoutCallback() {
        gUser = null;
        user=null;
        removeSp(Constants.SP_LOGIN_USER_TOKEN);
        removeSp(Constants.SP_LAST_LOGIN_USER_LOGIN);
        removeSp(Constants.SP_LAST_LOGIN_USER_INFO);
        User.deleteAll();
        if (getStompClient() != null) {
            getStompClient().disconnect();
            mSignalClient.disConnect();
            mStompClient = null;
        }
        mSignalClient.disConnect();
    }
    private Activity callActivity;

    public void setCallActivity(Activity callActivity) {
        this.callActivity = callActivity;
    }
    public Activity getCallActivity(){
       return callActivity;
    }
    /*   *//**
     * 保存用户信息
     *//*
    public void setUserInfo(JSONObject userInfo) {
        gUser = userInfo;
        setSp(Constants.SP_LAST_LOGIN_USER_INFO, userInfo.toString());
    }*/
    public void setUserInfo(String string) {
        user = gson.fromJson(string,User.class);
        setSp(Constants.SP_LAST_LOGIN_USER_INFO, string);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


}
