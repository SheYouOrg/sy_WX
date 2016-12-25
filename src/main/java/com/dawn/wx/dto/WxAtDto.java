package com.dawn.wx.dto;

/**
 * Created by DawnHeaven on 16-12-25.
 */
public class WxAtDto {
    private String access_token;

    private int expires_in;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }
}
