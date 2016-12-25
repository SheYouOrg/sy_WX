package com.dawn.wx.service;

import com.dawn.wx.entity.AccessToken;

import java.util.Date;

/**
 * Created by DawnHeaven on 16-12-25.
 */
public interface IAccessTokenService {

    AccessToken getAccessTokenByAt(String at);

    void saveAccessToken(String at, int expiresIn, boolean latest);

    void invalidAccessToken(String at, Date invalidTime, boolean latest);
}
