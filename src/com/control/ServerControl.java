package com.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JOptionPane;

import com.model.Message;
import com.model.User;
import com.view.MainForm;

public class ServerControl extends MainForm {

    private ServerSocket serverSocket;
    private DatagramSocket UDPsocket;

    //Status
    private boolean isStart = false;  //判断服务器是否已经启动

    //Threads
    //ArrayList<ClientServiceThread> clientServiceThreads;
    ConcurrentHashMap<String, ClientServiceThread> clientServiceThreads;
    ServerThread serverThread;
    UDPServerThread UDPThread;
    private byte[] buffer = new byte[1024];

    //构造函数
    public ServerControl() {
        startServer();
//        //发送按钮绑定点击事件
//        sendButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                sendAll();
//            }
//        });
        //绑定窗口关闭事件
        this.frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isStart) {
                    stopServer();
                }
                System.exit(0);
            }
        });
    }

    //启动服务端
    private void startServer() {

        if (Port < 1 || Port > 65535) {
            showErrorMessage("端口号必须在1～65535之间");
            return;
        }

        try {  //运用获取到的端口号开启服务器线程
            clientServiceThreads = new ConcurrentHashMap<String, ClientServiceThread>();
            UDPsocket = new DatagramSocket(Port);
            serverSocket = new ServerSocket(Port);
            UDPThread = new UDPServerThread();
            serverThread = new ServerThread();
            UDPThread.start();
            serverThread.start();
            isStart = true;
        } catch (BindException e) {
            isStart = false;
            showErrorMessage("启动服务器失败：端口被占用！");
            return;
        } catch (Exception e) {
            isStart = false;
            showErrorMessage("启动服务器失败：启动异常！");
            e.printStackTrace();
            return;
        }

//        serviceUISetting(true);
    }

    private synchronized void stopServer() {
        try {
            serverThread.closeThread();
            UDPThread.closeThread();
            //断开与所有客户端的连接
            for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {
                ClientServiceThread clientThread = entry.getValue();
                clientThread.sendMessage("CLOSE");
                clientThread.close();
            }

            clientServiceThreads.clear();
            listModel.removeAllElements();
            isStart = false;
//            serviceUISetting(false);
//            logMessage("服务器已关闭！");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("关闭服务器异常！");
            isStart = true;
//            serviceUISetting(true);
        }
    }

    private void sendAll() {
        if (!isStart) {
            showErrorMessage("服务器还未启动，不能发送消息！");
            return;
        }

        if (clientServiceThreads.size() == 0) {
            showErrorMessage("没有用户在线，不能发送消息！");
            return;
        }

//        String message = serverMessageTextField.getText().trim();
        String message = "Hahaha";
        if (message == null || message.equals("")) {
            showErrorMessage("发送消息不能为空！");
            return;
        }

        for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {
            entry.getValue().sendMessage("MSG@ALL@SERVER@" + message);
        }

//        logMessage("Server: " + message);
        chatBox.setText(null);
    }

    private void logMessage(String msg) {
        logTextArea.append(msg + "\r\n");
    }

    private void showErrorMessage(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private class UDPServerThread extends Thread {
        private boolean isRunning;

        public UDPServerThread() {
            this.isRunning = true;
        }

        public void run() {
            DatagramPacket packet;
            while (this.isRunning) {
                try {
                    if (!UDPsocket.isClosed()) {  //接收客户端发来的连接请求

                        packet = new DatagramPacket(buffer, buffer.length);

                        UDPsocket.receive(packet);
                        System.out.println("Receive!");
                        String name = new String(packet.getData(), 0, packet.getLength());
                        String Addr = packet.getAddress().getHostAddress();
                        int port = packet.getPort();

                        // 检查用户名和IP地址是否已经存在
                        if (onlineUsers.containsKey(name) && onlineUsers.get(name).getIpAddr().equals(Addr)) {
                            // 用户名和IP地址已存在，不需要再次添加
                            System.out.println("User " + name + " with IP " + Addr + " is already online.");
                        } else {
                            User newUser = new User(name, Addr);
                            onlineUsers.put(name, newUser); // 全局共享HashMap，键唯一
                            listModel.addElement(name);
                        }

                        // 检测到广播消息，发送用户名和IP信息
                        String clientMessage = Username;
                        byte[] data = clientMessage.getBytes();
                        InetAddress address = InetAddress.getByName(Addr);
                        DatagramPacket responsePacket = new DatagramPacket(data, data.length, address, port);
                        UDPsocket.send(responsePacket);
                        System.out.println("Sent response to server: " + clientMessage);

                        // 退出循环，因为我们只响应一次
                        break;

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized void closeThread() throws IOException {
            try {
                this.isRunning = false;
                serverSocket.close(); // 关闭 serverSocket，强行跳出 accept() 的阻塞状态
                System.out.println("serverSocket closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Server Thread class
    private class ServerThread extends Thread {
        private boolean isRunning;

        public ServerThread() {
            this.isRunning = true;
        }

        public void run() {
            while (this.isRunning) {
                try {
                    if (!serverSocket.isClosed()) {  //接收客户端发来的连接请求
                        Socket socket = serverSocket.accept();

                        ClientServiceThread clientServiceThread = new ClientServiceThread(socket);
                        User user = clientServiceThread.getUser();
                        clientServiceThreads.put(user.description(), clientServiceThread);
                        listModel.addElement(user.getName());
//                        logMessage(user.description() + "上线...");
                        clientServiceThread.start();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized void closeThread() throws IOException {
            try {
                this.isRunning = false;
                serverSocket.close(); // 关闭 serverSocket，强行跳出 accept() 的阻塞状态
                System.out.println("serverSocket closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Client Thread class
    private class ClientServiceThread extends Thread {
        private Socket socket;
        private User user;
        private BufferedReader reader;
        private PrintWriter writer;
        private boolean isRunning;

        private synchronized boolean init() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());
                String info = reader.readLine();
                StringTokenizer tokenizer = new StringTokenizer(info, "@");
                String type = tokenizer.nextToken();
                if (!type.equals("LOGIN")) {
                    sendMessage("ERROR@MESSAGE_TYPE");
                    return false;
                } else {
                    String name = tokenizer.nextToken();
                    InetAddress clientAddress = socket.getInetAddress();
                    String Addr = clientAddress.getHostAddress();
                    if (onlineUsers.containsKey(name)) {
                        user = onlineUsers.get(name);
                    } else {
                        User newUser = new User(name, Addr);
                        onlineUsers.put(name, newUser); // 全局共享HashMap，键唯一
                        listModel.addElement(name);
                        user = onlineUsers.get(name);
                    }
                }
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        public ClientServiceThread(Socket socket) {
            this.socket = socket;
            this.isRunning = init();
            if (!this.isRunning) {
//                logMessage("服务线程开启失败！");
            }
        }

        public void run() {
            while (isRunning) {
                try {
                    String message = reader.readLine();
                    System.out.println("Receive!");
                    StringTokenizer tokenizer = new StringTokenizer(message, "@");
                    String type = tokenizer.nextToken();
                    // System.out.println("recieve message: " + message);
                    if (message.equals("LOGOUT")) {
//                        logMessage(user.description() + "下线...");
                        //移除该用户以及服务器线程
//                        listModel.removeElement(user.getName());
                        clientServiceThreads.remove(user.description());
                        close();
                        return;
                    } else if (type.equals("text")) {
                        Message mess = new Message("text",user.getName(),tokenizer.nextToken());
                        user.addMessage(mess);
                    } else if (type.equals("audio")) {

                    } else if (type.equals("image")) {

                    } else if (type.equals("file")) {

                    }
                    //发送消息
//                        dispatchMessage(message);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        public void close() throws IOException {
            this.isRunning = false;
            this.reader.close();
            this.writer.close();
            this.socket.close();

        }

        public void sendMessage(String message) {
            writer.println(message);
            writer.flush();
        }

        public User getUser() {
            return user;
        }
    }

    //客户端主函数
//    public static void main(String args[]) {
//        new ServerMain();
//    }

}
