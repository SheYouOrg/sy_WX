package com.dawn.wx.controller;

import com.dawn.wx.entity.AccessToken;
import com.dawn.wx.service.IAccessTokenService;
import com.dawn.wx.service.WxService;
import com.dawn.wx.utils.ClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by DawnHeaven on 16-12-25.
 */
@Controller
public class IndexController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    IAccessTokenService atService;

    @Autowired
    WxService wxService;
    @RequestMapping("testAT")
    public String testAt(HttpServletRequest request,
                         ModelMap model){
        AccessToken accessToken = atService.getAccessTokenByAt("");
        if(accessToken == null){
            String result = wxService.getAccessToken();
            LOGGER.info("getAccessToken result---->{}", result);
            model.put("result", result);
        }
        return "test";
    }

    @RequestMapping("test")
    public String test(HttpServletRequest request,ModelMap model){
        model.addAttribute("result", "success");
        return "test/test";
    }
}
