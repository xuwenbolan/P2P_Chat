package com.model;

import com.model.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Yujie_Yang
 *  用户的模型定义
 */

public class User {  
    private String name;    //用户名
    private String ipAddr;  //IP地址
    private List<Message> mess;
      
    public User(String userDescription) {  
        String items[] = userDescription.split("%");  //把字符串用%分割
        this.name = items[0];    //第一部分赋给用户名
        this.ipAddr = items[1];  //第二部分赋给IP地址
        this.mess = new ArrayList<>();
    }  
  
    public User(String name, String ipAddr) {  
        this.name = name;  
        this.ipAddr = ipAddr;
        this.mess = new ArrayList<>();
    }

    // 添加消息到列表
    public void addMessage(Message message) {
        if (message != null) {
            this.mess.add(message);
        }
    }

    // 获取消息列表
    public List<Message> getMessages() {
        return mess;
    }
    public String getName() {  
        return name;  
    }  
  
    public String getIpAddr() {  
        return ipAddr;  
    }  
  
    public String description() {  
        return name + "%" + ipAddr;  //统一用 “用户名” + “%” + “IP地址” 的形式表示
    }  
}  
