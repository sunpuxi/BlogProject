package com.bolg.ws;

import lombok.Data;

/**
 * 发送消息的实体类
 */
@Data
public class Message {

    /**
     * 要发送给谁
     */
    private String toName;

    /**
     * 消息的内容
     */
    private String message;
}
