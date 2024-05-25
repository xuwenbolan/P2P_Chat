package com.model;

import java.time.LocalDateTime;

public class Message {
    private String owner;
    private LocalDateTime time; // 消息发送的时间
    private String type; // 消息类型，例如 "text", "audio", "image", "file"
    private String content; // 消息内容或文件路径

    // 构造函数，用于文本消息
    public Message(String type,String owner, String content) {
        this.type = type;
        this.owner = owner;
        this.content = content;
        this.time = LocalDateTime.now(); // 记录当前时间
    }

    // 获取消息发送时间
    public LocalDateTime getTime() {
        return time;
    }

    // 设置消息发送时间
    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    // 获取消息类型
    public String getType() {
        return type;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
    // 设置消息类型
    public void setType(String type) {
        this.type = type;
    }

    // 获取消息内容或文件路径
    public String getContent() {
        return content;
    }

    // 设置消息内容或文件路径
    public void setContent(String content) {
        this.content = content;
    }

    // 可以添加其他方法，例如 toString() 用于返回消息的字符串表示
    @Override
    public String toString() {
        return "Message{" +
                "time=" + time +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}