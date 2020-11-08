package com.preschool.edu.core;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

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
            this.code = payload.optInt("status");
            this.returnMsg = payload.optString("title");
            this.payload = payload.opt("body");
        }
        if (StringUtils.isBlank(this.returnMsg)) {
            this.returnMsg = "未知错误";
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
