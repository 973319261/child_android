package com.android.core;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

/**
 * 请求返回
 */
public final class HttpResult {
    public static final int TimeOutCode = 404;
    public JSONObject rawJson;
    public Object payload;
    public String returnMsg;
    public int code;

    private HttpResult(JSONObject payload) {
        this.rawJson = payload;
        if (null == payload) {
            this.code = 201;
            this.payload = null;
        } else {
            this.code = payload.optInt("code");
            this.returnMsg = payload.optString("msg");
            this.payload = payload.opt("data");
        }
        if (StringUtils.isBlank(this.returnMsg)) {
            this.returnMsg = "error";
        }
    }

    public static HttpResult createWith(JSONObject jsonObject) {
        if (jsonObject == null) {
            return new HttpResult(null);
        } else {
            HttpResult result = new HttpResult(jsonObject);
            return result;
        }
    }

    public boolean isSuccess() {
        return this.code == 200;
    }
}
