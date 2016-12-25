package com.dawn.wx.service;

import com.dawn.wx.utils.ClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by DawnHeaven on 16-12-25.
 */
 @Service
public class WxService {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    /*@Value("WX_AppID")
    private String appId;
    @Value("WX_AppSecret")
    private String appSecret;*/

    private static final String appId = "wxc28a9120f32e58f8";

    private static final String appSecret = "75bf4ca8f0ff7a2d1b26959316e192f1";
    public String getAccessToken(){
        String url = "https://api.weixin.qq.com/cgi-bin/token?" +
                "grant_type=client_credential&appid=" + appId + "&secret=" + appSecret;

        LOGGER.info("get AccessToken Url ----> {}", url);
        String result = ClientUtils.doGet(url, null);

        return result;
    }
}
