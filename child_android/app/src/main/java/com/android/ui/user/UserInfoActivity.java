package com.android.ui.user;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.alibaba.fastjson.JSON;
import com.android.AppT;
import com.android.PEApplication;
import com.android.bean.User;
import com.android.core.Constants;
import com.android.core.HttpResult;
import com.android.core.HttpUtil;
import com.android.dialog.InputDialog;
import com.android.ui.BaseActivity;
import com.android.utils.DensityUtil;
import com.android.utils.GetImagePath;
import com.android.utils.GlideRoundTransformUtil;
import com.android.utils.MPermissionUtils;
import com.android.utils.OkHttpTool;
import com.android.utils.StatusBarUtil;
import com.android.utils.Tools;
import com.android.widgets.DialogProgress;
import com.android.widgets.MyActionBar;
import com.bryant.selectorlibrary.DSelectorPopup;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.koi.chat.R;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.leefeng.promptlibrary.PromptDialog;

/**
 * 编辑用户信息页面
 */
public class UserInfoActivity extends AppCompatActivity {
    private Gson gson=new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private Activity myActivity;//上下文
    private MyActionBar myActionBar;//标题栏
    private TextView tvNickName;
    private TextView tvGender;
    private TextView tvSignature;
    private LinearLayout llAvatar;
    private LinearLayout llNickName;
    private LinearLayout llGender;
    private LinearLayout llSignature;
    private ImageView ivAvatar;
    private User user;
    private PromptDialog promptDialog;//加载框
    private static final int IMAGE_REQUEST_CODE = 100;
    private static final int IMAGE_REQUEST_CODE_GE7 = 101;
    private static final int CAMERA_REQUEST_CODE = 104;
    private File mGalleryFile;//存放图库选择是返回的图片
    private File mCropFile;//存放图像裁剪的图片
    private DialogProgress proDialog;
    private String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        myActionBar=findViewById(R.id.myActionBar);
        tvNickName=findViewById(R.id.tv_user_nickName);
        tvGender=findViewById(R.id.tv_user_gender);
        tvSignature=findViewById(R.id.tv_user_signature);
        llAvatar=findViewById(R.id.ll_user_avatar);
        llNickName=findViewById(R.id.ll_user_nickName);
        llGender=findViewById(R.id.ll_user_gender);
        llSignature=findViewById(R.id.ll_user_signature);
        ivAvatar=findViewById(R.id.iv_user_avatar);
        myActivity=this;
        myActionBar.setData(getString(R.string.text_user_info_title), R.drawable.ic_custom_actionbar_left_black, "", 1, 0, new MyActionBar.ActionBarClickListener() {
            @Override
            public void onLeftClick() {
                finish();
            }

            @Override
            public void onRightClick() {

            }
        });
        initView();//初始化页面
        setViewListener();//设置监听事件
    }
    private void initView(){
        StatusBarUtil.setStatusBar(this,true);//设置当前界面是否是全屏模式（状态栏）
        StatusBarUtil.setStatusBarLightMode(this,true);//状态栏文字颜色
        String path = PEApplication.INSTANCE.getCacheDir().getAbsolutePath();
        //相册的File对象
        mGalleryFile = new File(path, "IMAGE_GALLERY_NAME.jpg");
        //裁剪后的File对象
        mCropFile = new File(path, "PHOTO_FILE_NAME.jpg");
        user= PEApplication.INSTANCE.getUser();
        tvNickName.setText(user.getName());
        tvGender.setText("0".equals(user.getSex())?getString(R.string.text_user_sex_man):getString(R.string.text_user_sex_woman));
        tvSignature.setText(user.getRemark());
        proDialog=new DialogProgress(myActivity);
        //获取头像信息
        url=Constants.AVATAR+user.getHeadPortraitsUrl();
        //设置头像加载失败时的默认头像
        Glide.with(myActivity)
                .load(url)
                .transform(new GlideRoundTransformUtil(myActivity, DensityUtil.dip2px(myActivity, 3)))
                .error(R.drawable.ic_avatar_error)
                .into(ivAvatar);
    }
    //查看头像
    private ImageView getView() {
        final ImageView imgView = new ImageView(this);
        Glide.with(this)
                .load(url)
                .fitCenter()
                .placeholder(R.mipmap.default_user_avatar)
                .into(imgView);
        return imgView;
    }
    private void setViewListener(){
        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 全屏显示的方法
                final Dialog dialog = new Dialog(myActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                ImageView imgView = getView();
                dialog.setContentView(imgView);
                dialog.show();
                imgView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });
        //头像
        llAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] permissions=new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};//写入存储权限
                if (MPermissionUtils.checkPermissions(myActivity, permissions)) {//检查是否有权限
                    //访问系统图库
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);//设置打开文件的模式 读取
                    intent.setType("image/*");//告诉系统我要获取图片
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //Android7.0及以上
                        Uri uriForFile = FileProvider.getUriForFile(myActivity,
                                "com.koi.chat.fileprovider", mGalleryFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        //启动页面，设置请求码
                        startActivityForResult(intent, IMAGE_REQUEST_CODE_GE7);
                    } else {
                        //Android7.0一下
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mGalleryFile));
                        startActivityForResult(intent, IMAGE_REQUEST_CODE);
                    }
                }else {//没有权限
                    MPermissionUtils.showTipsDialog(myActivity,getString(R.string.text_permissions_storage));//提示
                }
            }
        });
        //昵称
        llNickName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final InputDialog inputDialog = new InputDialog(getString(R.string.text_user_info_nickname),tvNickName.getText().toString(), InputType.TYPE_CLASS_TEXT,15,new InputDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(final Dialog dialog, final String content) {
                        if (!tvNickName.getText().toString().equals(content)){
                            user.setName(content);
                            proDialog.show();
                            final RequestParams params = HttpUtil.requestParams("users/profile");
                            OkHttpTool.httpPostJson(params.toString(), gson.toJson(user), new OkHttpTool.ResponseCallback() {
                                @Override
                                public void onResponse(final boolean isSuccess, final int responseCode, final String response, Exception exception) {
                                    myActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                            if (isSuccess && responseCode==200){
                                                Log.i("response",response);
                                                try {
                                                    JSONObject jsonObject=new JSONObject(response);
                                                    HttpResult httpResult = HttpResult.createWith(jsonObject);
                                                    if (httpResult.isSuccess()) {
                                                        tvNickName.setText(content);
                                                        PEApplication.INSTANCE.setUserInfo(gson.toJson(user));//更新传递的信息
                                                        Toast.makeText(myActivity, R.string.text_user_info_update_succeed,Toast.LENGTH_LONG).show();
                                                    } else {
                                                        Toast.makeText(myActivity,httpResult.returnMsg,Toast.LENGTH_LONG).show();
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                            }else {
                                                Toast.makeText(myActivity, R.string.text_user_info_update_failure,Toast.LENGTH_LONG).show();
                                            }
                                            proDialog.hide();
                                        }
                                    });
                                }
                            });
                        }else {//没有修改
                            Toast.makeText(myActivity, R.string.text_user_info_nickname_agreed,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                inputDialog.show(getSupportFragmentManager(),"");

            }
        });
        //性别
        llGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> listName=new ArrayList<>();
                listName.add(getString(R.string.text_user_sex_man));
                listName.add(getString(R.string.text_user_sex_woman));
                final DSelectorPopup dSelectorPopup=new DSelectorPopup(myActivity,listName);
                dSelectorPopup.setTextcolor_unchecked(getResources().getColor(R.color.colorPrimary))
                        .setOffset(user.getSex()==null?0: Integer.valueOf(user.getSex()))//默认选中
                        .setButtonText(getString(R.string.text_ok))
                        .setGradual_color(0xffD81B60)
                        .setTitleText(getString(R.string.text_user_info_sex))
                        .setTitleColor(getResources().getColor(R.color.colorPrimary)).build();
                dSelectorPopup.popOutShadow(v);
                dSelectorPopup.setSelectorListener(new DSelectorPopup.SelectorClickListener() {
                    @Override
                    public void onSelectorClick(int position, String text) {
                        int index=text==getString(R.string.text_user_sex_man)?0:1;
                        user.setSex(String.valueOf(index));
                        proDialog.show();
                        final RequestParams params = HttpUtil.requestParams("users/profile");
                        OkHttpTool.httpPostJson(params.toString(), gson.toJson(user), new OkHttpTool.ResponseCallback() {
                            @Override
                            public void onResponse(final boolean isSuccess, final int responseCode, final String response, Exception exception) {
                                myActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isSuccess && responseCode==200){
                                            Log.i("response",response);
                                            try {
                                                JSONObject jsonObject=new JSONObject(response);
                                                HttpResult httpResult = HttpResult.createWith(jsonObject);
                                                if (httpResult.isSuccess()) {
                                                    PEApplication.INSTANCE.setUserInfo(gson.toJson(user));//更新传递的信息
                                                    tvGender.setText("0".equals(user.getSex())?getString(R.string.text_user_sex_man):getString(R.string.text_user_sex_woman));
                                                    Toast.makeText(myActivity,R.string.text_user_info_update_succeed,Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(myActivity,httpResult.returnMsg,Toast.LENGTH_LONG).show();
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                        }else {
                                            Toast.makeText(myActivity,R.string.text_user_info_update_failure,Toast.LENGTH_LONG).show();
                                        }
                                        proDialog.hide();
                                        dSelectorPopup.dismissPopup();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
        //个性签名
        llSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final InputDialog inputDialog = new InputDialog(getString(R.string.text_user_info_signature),tvSignature.getText().toString(), InputType.TYPE_CLASS_TEXT,25,new InputDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(final Dialog dialog, final String content) {
                        if (!tvSignature.getText().toString().equals(content)){
                            user.setRemark(content);
                            proDialog.show();
                            final RequestParams params = HttpUtil.requestParams("users/profile");
                            OkHttpTool.httpPostJson(params.toString(), gson.toJson(user), new OkHttpTool.ResponseCallback() {
                                @Override
                                public void onResponse(final boolean isSuccess, final int responseCode, final String response, Exception exception) {
                                    myActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (isSuccess && responseCode==200){
                                                Log.i("response",response);
                                                try {
                                                    JSONObject jsonObject=new JSONObject(response);
                                                    HttpResult httpResult = HttpResult.createWith(jsonObject);
                                                    if (httpResult.isSuccess()) {
                                                        tvSignature.setText(content);
                                                        PEApplication.INSTANCE.setUserInfo(gson.toJson(user));//更新传递的信息
                                                        Toast.makeText(myActivity,R.string.text_user_info_update_succeed,Toast.LENGTH_LONG).show();
                                                    } else {
                                                        Toast.makeText(myActivity,httpResult.returnMsg,Toast.LENGTH_LONG).show();
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                            }else {
                                                Toast.makeText(myActivity,R.string.text_user_info_update_failure,Toast.LENGTH_LONG).show();
                                            }
                                            dialog.dismiss();
                                            proDialog.hide();
                                        }
                                    });
                                }
                            });
                        }else {//没有修改
                            Toast.makeText(myActivity,R.string.text_user_info_update_failure,Toast.LENGTH_LONG).show();
                        }
                    }
                });
                inputDialog.show(getSupportFragmentManager(),"");

            }
        });
    }
    /**
     * 返回图片
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && (data != null || requestCode == CAMERA_REQUEST_CODE)) {
            switch (requestCode) {
                case IMAGE_REQUEST_CODE://版本<7.0  图库返回
                    //获取图片的全路径
                    Uri uri = data.getData();
                    //进行图像裁剪
                    startPhotoZoom(uri);
                    break;
                case IMAGE_REQUEST_CODE_GE7://版本>= 7.0 图库返回
                    //获取文件路径
                    String strPath = GetImagePath.getPath(myActivity, data.getData());
                    if (Tools.isNotNull(strPath)) {
                        File imgFile = new File(strPath);
                        //通过FileProvider创建一个content类型的Uri
                        Uri dataUri = FileProvider.getUriForFile(myActivity, "com.koi.chat.fileprovider", imgFile);
                        //进行图像裁剪
                        startPhotoZoom(dataUri);
                    } else {
                        Toast.makeText(myActivity, "选择图片失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case UCrop.REQUEST_CROP://Ucrop裁剪返回
                    Uri resultUri = UCrop.getOutput(data);
                    if (resultUri != null) {
                        //uri转文件路径
                        String strPathCrop = GetImagePath.getPath(myActivity, resultUri);
                        if (Tools.isNotNull(strPathCrop)) {
                            File fileUp = new File(strPathCrop);
                            //保存到服务器
                            if (fileUp.exists()) {
                                //=====上传文件
                                final RequestParams params = HttpUtil.requestParams("uploadAvatar");
                                //参数map
                                Map<String, Object> map = new HashMap<>();
                                //文件map
                                Map<String, File> fileMap = new HashMap<>();
                                fileMap.put("file", fileUp);
                                proDialog.show();
                                //发送请求
                                OkHttpTool.httpPostWithFile(params.toString(), map, fileMap, new OkHttpTool.ResponseCallback() {
                                    @Override
                                    public void onResponse(final boolean isSuccess, final int responseCode, final String response, Exception exception) {
                                        myActivity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (isSuccess && responseCode == 200) {
                                                    try {
                                                        JSONObject jsonObject=new JSONObject(response);
                                                        HttpResult httpResult = HttpResult.createWith(jsonObject);
                                                        if (httpResult.isSuccess()) {
                                                            //获取头像信息
                                                            String url=Constants.AVATAR+httpResult.payload;
                                                            //设置头像加载失败时的默认头像
                                                            Glide.with(myActivity)
                                                                    .load(url)
                                                                    .transform(new GlideRoundTransformUtil(myActivity, DensityUtil.dip2px(myActivity, 3)))
                                                                    .error(R.drawable.ic_avatar_error)
                                                                    .into(ivAvatar);
                                                            user.setHeadPortraitsUrl(httpResult.payload.toString());
                                                            PEApplication.INSTANCE.setUserInfo(gson.toJson(user));//更新传递的信息
                                                            Toast.makeText(myActivity,"上传成功",Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(myActivity,httpResult.returnMsg,Toast.LENGTH_SHORT).show();
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }else {
                                                    Toast.makeText(myActivity,R.string.text_user_info_update_failure,Toast.LENGTH_LONG).show();
                                                }
                                                //关闭加载层
                                                proDialog.hide();
                                            }
                                        });
                                    }
                                });
                                return;
                            }
                        }
                    }
                    Toast.makeText(myActivity, "图片裁剪失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    //权限请求的回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MPermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    /**
     * 开始图片裁剪 使用UCrop
     *
     * @param uri uri地址
     */
    private void startPhotoZoom(Uri uri) {
        //裁剪后保存到文件中
        Uri cropFileUri = Uri.fromFile(mCropFile);
        UCrop uCrop = UCrop.of(uri, cropFileUri);//源文件url,裁剪后输出文件uri
        UCrop.Options options = new UCrop.Options();
        //设置裁剪图片可操作的手势
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        //是否能调整裁剪框
        options.setFreeStyleCropEnabled(false);
        uCrop.withOptions(options);
        //设置比例为1:1
        uCrop.withAspectRatio(1, 1);
        //注意！！！！Fragment中使用uCrop 必须这样，否则Fragment的onActivityResult接收不到回调
        uCrop.start((AppCompatActivity) myActivity);
    }
}
