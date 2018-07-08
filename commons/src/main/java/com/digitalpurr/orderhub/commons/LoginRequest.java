package com.digitalpurr.orderhub.commons;

import com.google.gson.annotations.SerializedName;

public class LoginRequest extends BaseRequest {
    @SerializedName("l") String login;

    public LoginRequest(String login) {
        this.id = RequestId.LOGIN;
        this.login = login;
    }
}
