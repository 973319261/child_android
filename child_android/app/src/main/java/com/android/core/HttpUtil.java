package com.android.core;

import com.android.utils.ALog;
import com.android.PEApplication;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.xutils.common.util.KeyValue;
import org.xutils.ex.HttpException;
import org.xutils.http.RequestParams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 请求工具类
 */
public class HttpUtil {

    public static RequestParams requestParams(String method) {
        String url = String.format("%s%s", Constants.HOST, method);
        ALog.e("url--->" + url);
        RequestParams params = new RequestParams(url);
        if (PEApplication.INSTANCE.isLogin() && StringUtils.isNotBlank(PEApplication.INSTANCE.getUserToken())) {
            params.setHeader("Authorization", String.format("Bearer %s", PEApplication.INSTANCE.getUserToken()));
        }

        return params;
    }

    public static void onError(String uri, Throwable ex) {
        if (ex instanceof HttpException) { // 网络错误
            HttpException httpEx = (HttpException) ex;
            int responseCode = httpEx.getCode();
            String responseMsg = httpEx.getMessage();
            String errorResult = httpEx.getResult();
            ALog.e("uri:" + uri + " responseCode:" + responseCode + "[responseMsg:" + responseMsg + ",errorResult:" + errorResult + "]");
        } else { // 其他错误
            ALog.e("uri:" + uri + " other error:" + ex.getMessage());
        }
    }

    public static void printUrl(RequestParams request) {
        StringBuffer params = new StringBuffer(request.getUri()).append("?");
        for (KeyValue keyValue : request.getBodyParams()) {
            params.append(keyValue.key).append("=").append(keyValue.getValueStr()).append("&");
        }
        ALog.e(params.toString());

        StringBuffer headerParams = new StringBuffer();
        for (KeyValue header : request.getHeaders()) {
            headerParams.append(header.key).append("=").append(header.getValueStr()).append("&");
        }
        ALog.e("headerParams：" + headerParams.toString());
    }

    public static String uploadImg(JSONArray images) {
        HttpClient httpClient = new DefaultHttpClient();
        String url = "";
        HttpPost post = new HttpPost(url);
        String result = "";
        try {
            StringEntity postingString = new StringEntity(images.toString());
            post.setEntity(postingString);
            post.setHeader("Content-type", "application/json");

            HttpResponse response = httpClient.execute(post);
            result = EntityUtils.toString(response.getEntity());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ALog.e("upload img datas:" + images);
        ALog.e("upload img result:" + result);
        return result;
    }


}
