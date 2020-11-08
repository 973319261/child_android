package com.preschool.edu;

import android.content.Context;


import androidx.multidex.BuildConfig;
import androidx.multidex.MultiDex;

import com.alibaba.fastjson.JSON;
import com.android.AppCc;
import com.android.utils.ALog;
import com.android.utils.AppUtil;
import com.android.utils.DateUtil;
import com.android.utils.ImageUtil;
import com.preschool.edu.core.Constants;
import com.preschool.edu.core.HttpResult;
import com.preschool.edu.core.HttpUtil;
import com.preschool.edu.model.Message;
import com.preschool.edu.model.MessageEvent;
import com.android.ForegroundCallbacks;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;

import org.apache.commons.lang.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.java_websocket.WebSocket;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.common.Callback;
import org.xutils.db.table.TableEntity;
import org.xutils.http.RequestParams;
import org.xutils.x;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;


/**
 * Created by jac_cheng on 2017/4/13.
 */
public class PEApplication extends AppCc {
    public static PEApplication INSTANCE;

    private JSONObject gUser;
    public static DbManager.DaoConfig daoConfig;

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

    private StompClient mStompClient;

    public void connectStompClient() {
        if (!AppUtil.isNetConnect()) {
            return;
        }
        if (!isLogin()) {
            return;
        }
        if (mStompClient == null) {
            initStompClient();
        }
    }

    public StompClient getStompClient() {
        connectStompClient();
        return mStompClient;
    }

    private void initStompClient() {
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
                        mStompClient = null;
                        ALog.e("连接关闭 Stomp connection closed");
                        break;
                }
            }
        });
    }

    private void registerStompTopic() {
        String topic = String.format("/topic/chat/oneToOne/%s", getUserLogin());
        ALog.e("registerStompTopic topic : "+ topic);
        getStompClient().topic(topic).subscribe(new Action1<StompMessage>() {
            @Override
            public void call(StompMessage stompMessage) {
                Message msg = JSON.parseObject(stompMessage.getPayload(), Message.class);
                msg.setId(0);
                msg.setSendTime(DateUtil.formatAllDateTime(System.currentTimeMillis()));
                msg.save();
                ALog.e("接受到消息: " + msg.toString());
                EventBus.getDefault().post(new MessageEvent(Constants.EVENT_MSG_TYPE_CHAT_RECEIVED_MSG, msg));
            }
        });
    }

    public JSONObject getUserInfo() {
        if (gUser == null) {
            gUser = AppUtil.toJsonObject(getSp(Constants.SP_LAST_LOGIN_USER_INFO, ""));
        }
        return gUser;
    }

    //用户登录名
    public String getUserLogin() {
        String login = getUserInfo().optString("login");
        if (StringUtils.isBlank(login)) {
            login = AppUtil.toJsonObject(getSp(Constants.SP_LAST_LOGIN_USER_LOGIN, "")).optString("login");
        }
        return login;
    }

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

    public String getUserLoginId() {
        JSONObject login = AppUtil.toJsonObject(getSp(Constants.SP_LAST_LOGIN_USER_LOGIN, ""));
        return login.optString("userLoginId");
    }

    public static boolean isEn() {
        return false;
    }

    public boolean isLogin() {
        return StringUtils.isNotBlank(getUserLoginId());
    }

    public String getUserToken() {
        return getSp(Constants.SP_LOGIN_USER_TOKEN, "");
    }

    public void userLoginCallback(final JSONObject userInfo) {
        setSp(Constants.SP_LOGIN_USER_TOKEN, userInfo.optString("idToken"));
        setSp(Constants.SP_LAST_LOGIN_USER_LOGIN, userInfo.toString());
        tryGetUserInfo(getUserLogin());
        initStompClient();
    }

    public void tryGetUserInfo(String mobile) {
        final RequestParams params = HttpUtil.requestParams(String.format("users/%s/profile", mobile));
        x.http().get(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                HttpResult httpResult = HttpResult.createWith(jsonObject);
                if (httpResult.isSuccess()) {
                    setUserInfo((JSONObject) httpResult.payload);
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

    public void userLogoutCallback() {
        gUser = null;
        removeSp(Constants.SP_LOGIN_USER_TOKEN);
        removeSp(Constants.SP_LAST_LOGIN_USER_LOGIN);
        removeSp(Constants.SP_LAST_LOGIN_USER_INFO);
        if (getStompClient() != null) {
            getStompClient().disconnect();
            mStompClient = null;
        }
    }

    public void setUserInfo(JSONObject userInfo) {
        gUser = userInfo;
        setSp(Constants.SP_LAST_LOGIN_USER_INFO, userInfo.toString());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
