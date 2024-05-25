package com.control;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import com.model.User;
import com.view.MainForm;

public class ClientControl extends MainForm {

    //model
    private User me;
    private String sendTarget = "ALL";  //默认发送对象

    //Socket
    private Socket socket;
    private PrintWriter writer;    //输出流
    private BufferedReader reader; //输入流


    //Status
    private boolean isConnected;   //判断是否连接到服务端

    //构造函数
    public ClientControl() {

        // 写消息的文本框中按回车键时事件
        messageTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });

        // 单击发送按钮时事件
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });

        // 关闭窗口时事件
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isConnected) {
                    disconnect();
                }
                System.exit(0);
            }
        });

        // 为在线用户添加点击事件
        userList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if(isConnected)disconnect();
                int index = userList.getSelectedIndex();  //获取被点击的用户的序号
                if (index < 0) return;
                if (index == 0) {  //默认为所有人
                    sendTarget = "ALL";
                }
                else {
                    String name = (String)listModel.getElementAt(index);  //获取被点击用户的名字
                    if (onlineUsers.containsKey(name)) {
                        User user = onlineUsers.get(name);
                        connect(user.getIpAddr());
                    } else {
                        sendTarget = "ALL";
                    }
                }
            }
        });
    }

    //连接
    private void connect(String ip) {

        if (ip == null || ip.equals("")) {  //判断IP地址是否为空
            showErrorMessage("IP地址不能为空！");
            return;
        }

        try {
            listModel.addElement("所有人");

            me = new User(Username, ip);
            socket = new Socket(ip, Port);  //根据指定IP地址以及端口号建立线程
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));  //输入流
            writer = new PrintWriter(socket.getOutputStream());  //输出流

            String myIP = socket.getLocalAddress().toString().substring(1);  //获取客户端所在的IP地址
            sendMessage("LOGIN@" + Username);  //发送用户登录信息
            isConnected = true;

        } catch(Exception e) {
            isConnected = false;
//            logMessage("客户端连接失败");
            listModel.removeAllElements();  //移除在线面板上所有用户
            e.printStackTrace();
            return;
        }

//        logMessage("客户端连接成功");       //将连接成功的消息显示到消息面板上
//        serviceUISetting(isConnected); //设置按钮的状态
    }

    //消息发送
    private void send() {
        if (!isConnected) {
            showErrorMessage("未连接到服务器！");
            return;
        }
        String message = messageTextField.getText().trim();  //获取发送框内容
        if (message == null || message.equals("")) {
            showErrorMessage("消息不能为空！");
            return;
        }
        try {
            //向服务器发送消息
            //MSG@+“接收消息用户名 %IP地址”+“发送者用户名 %IP地址”+@+message
            sendMessage("text@" + message);
//            logMessage("我->" + to + ": " + message);
        } catch(Exception e) {
            e.printStackTrace();
//            logMessage("（发送失败）我->" + to + ": " + message);
        }

        messageTextField.setText(null);  //发送完毕把输入框置空
    }

    //断开连接
    private synchronized void disconnect() {
        try {
            //向服务器发送断开连接的消息
            sendMessage("LOGOUT");
//            listModel.removeAllElements();
//            onlineUsers.clear();

            reader.close();
            writer.close();
            socket.close();
            isConnected = false;
//            serviceUISetting(false);

//            sendTarget = "ALL";
//            messageToLabel.setText("To: 所有人");

//            logMessage("已断开连接...");
        } catch(Exception e) {
            e.printStackTrace();
            isConnected = true;
//            serviceUISetting(true);
            showErrorMessage("服务器断开连接失败！");
        }
    }

    private void sendMessage(String message) {
        writer.println(message);
        writer.flush();
    }

    private void logMessage(String msg) {
        chatBox.append(msg + "\r\n");
    }

    private void showErrorMessage(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // 主函数
//    public static void main(String args[]){
//        new ClientMain();
//    }
}
