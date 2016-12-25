package com.dawn.wx.dao;

import com.dawn.wx.entity.AccessToken;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * Created by DawnHeaven on 16-12-25.
 */
public interface AccessTokenDao {

    AccessToken getByAt(@Param("at") String at);

    int saveAccessToken(@Param("at") String at, @Param("expiresIn") int expiresIn, @Param("latest") boolean lastest);

    int invalidAccessToken(@Param("at") String at, @Param("invalidTime")Date invalidTime, @Param("latest") boolean latest);
}
