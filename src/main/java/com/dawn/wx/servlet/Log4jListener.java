package com.dawn.wx.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by DawnHeaven on 16-12-23.
 */
public class Log4jListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(Log4jListener.class);
    public static final String LOG4J_DIR = "log4jdir";

    //日志文件位置设置
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        WebApplicationContext ctx =
                WebApplicationContextUtils.getWebApplicationContext(context);
        LOGGER.info("WebApplicationContext", ctx);
        String log4jdir = event.getServletContext().getRealPath("/");
        System.setProperty(LOG4J_DIR, log4jdir);
        LOGGER.info(LOG4J_DIR  + " = {}", log4jdir );
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.getProperties().remove(LOG4J_DIR);
    }
}
