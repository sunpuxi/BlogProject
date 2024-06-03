package com.bolg.ws;

import com.alibaba.fastjson.JSON;
import com.bolg.constant.UserConstant;
import com.bolg.model.entity.User;
import com.bolg.config.GetHttpSessionConfig;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EndPoint 对象，指明需要访问的路径
 */
@ServerEndpoint(value = "/chat",configurator = GetHttpSessionConfig.class)
@Component
public class ChatEndPoint {

    /**
     * 保存在线的用户的 session 信息,声明为静态的原因：
     * ChatEndPoint 是多例的 Bean，每一个 BEAN 都有一个存储在线用户 session 信息的集合，这是不符合需求的。
     */
    private static final Map<String,Session> userMap = new ConcurrentHashMap<>();

    private HttpSession httpSession;

    /**
     * 当 WebSocket 连接建立好之后会执行的方法
     * @param session
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig){
        //保存 session
        this.httpSession = (HttpSession) endpointConfig.getUserProperties().get(HttpSession.class.getName());
        User user = (User) this.httpSession.getAttribute(UserConstant.USER_LOGIN_STATE);
        userMap.put(user.getUserName(),session);
        //消息广播
        broadcastAllUsers(user.getUserName()+"已上线");
    }

    /**
     * 广播消息
     * @param message
     */
    public void broadcastAllUsers(String message){
        //遍历 map 集合
        Set<Map.Entry<String, Session>> entries = userMap.entrySet();
        for (Map.Entry<String, Session> entry : entries) {
            Session session = entry.getValue();
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 浏览器发送消息至服务器时，调用的方法
     * @param message
     */
    @OnMessage
    public void onMessage(String message ) throws IOException {
        Message messageEntity = JSON.parseObject(message, Message.class);
        String toName = messageEntity.getToName();
        String messageContent = messageEntity.getMessage();
        //获取要发送的目标用户的用户 session
        Session session = userMap.get(toName);
        session.getBasicRemote().sendText(messageContent);
    }

    /**
     * 断开 websocket 连接时调用的方法
     * @param session
     */
    @OnClose
    public void onClose(Session session){
        //从 userMap 中剔除当前登录的用户的 session 信息
        User user = (User) this.httpSession.getAttribute(UserConstant.USER_LOGIN_STATE);
        userMap.remove(user.getUserName());
        //通知其他用户，当前用户已经下线
        broadcastAllUsers(user.getUserName() + "用户已经下线");
    }

}
