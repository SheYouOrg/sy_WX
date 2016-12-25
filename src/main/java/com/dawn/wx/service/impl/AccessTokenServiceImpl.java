package com.dawn.wx.service.impl;

import com.dawn.wx.dao.AccessTokenDao;
import com.dawn.wx.entity.AccessToken;
import com.dawn.wx.service.IAccessTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by DawnHeaven on 16-12-25.
 */
@Service
public class AccessTokenServiceImpl implements IAccessTokenService
{

    @Autowired
    AccessTokenDao atDao;

    @Override
    public AccessToken getAccessTokenByAt(String at) {
        return atDao.getByAt(at);
    }

    @Override
    public void saveAccessToken(String at, int expiresIn, boolean latest) {
        atDao.saveAccessToken(at, expiresIn, latest);
    }

    @Override
    public void invalidAccessToken(String at, Date invalidTime, boolean latest) {
        atDao.invalidAccessToken(at, invalidTime, latest);
    }
}
