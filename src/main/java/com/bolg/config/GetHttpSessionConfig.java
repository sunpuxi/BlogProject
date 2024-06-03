package com.bolg.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * 获取 HttpSession 的配置类
 */
public class GetHttpSessionConfig extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        //获取 httpsession 对象
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        //保存 httpsession 对象
        sec.getUserProperties().put(httpSession.getClass().getName(),httpSession);
    }
}
