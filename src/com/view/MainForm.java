package com.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainForm extends JFrame {
    final private JComboBox<String> chatPersonComboBox;
    final private JButton  configButton;
    final private JTextArea chatBox;
    final private JTextField messageTextField;
    final private JButton sendButton;
    final private JButton connectDisconnectButton; // 合并为一个按钮

    private boolean isConnected = false; // 跟踪连接状态
    private boolean hasName = false; // 判断有无输入名字
    private JButton serverConfigButton;

    public MainForm() {
        setTitle("聊天界面");
        setSize(880, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 聊天人下拉选择框
        chatPersonComboBox = new JComboBox<>();
        chatPersonComboBox.addItem("聊天人1");
        chatPersonComboBox.addItem("聊天人2");
        chatPersonComboBox.addItem("聊天人3");
        add(chatPersonComboBox, BorderLayout.NORTH);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());


        configButton = new JButton("用户配置");

        // 连接/断开按钮
        connectDisconnectButton = new JButton("连接/断开");
        connectDisconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isConnected) {
                    connect();
                } else {
                    disconnect();
                }
            }
        });
        updateButtonState(false);

        //配置按钮
        buttonPanel.add(connectDisconnectButton);
        buttonPanel.add(configButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // 聊天框
        chatBox = new JTextArea();
        chatBox.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatBox);
        add(scrollPane, BorderLayout.CENTER);

        // 文本输入域和发送按钮
        JPanel messagePanel = new JPanel();
        JPanel messagePanel2 = new JPanel();
        messagePanel.setLayout(new BorderLayout());
        messagePanel2.setLayout(new FlowLayout());

        messageTextField = new JTextField(20);
        sendButton = new JButton("发送");


        messagePanel.add(messagePanel2,BorderLayout.SOUTH);
        messagePanel.add(chatPersonComboBox,BorderLayout.NORTH);
        messagePanel2.add(messageTextField);
        messagePanel2.add(sendButton);
        add(messagePanel, BorderLayout.EAST);

        serverConfigButton = new JButton("服务器配置");
        buttonPanel.add(serverConfigButton);
        serverConfigButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showServerConfigDialog();
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                sendMessage();
            }
        });
        configButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showConfigDialog();
            }
        });

        setVisible(true);
    }
    private void connect() {
        // 连接逻辑
        isConnected = true;
        updateButtonState(true);
        chatBox.append("已连接到聊天人: " + chatPersonComboBox.getSelectedItem() + "\n");
    }

    private void disconnect() {
        // 断开逻辑
        isConnected = false;
        updateButtonState(false);
        chatBox.append("已断开连接\n");
    }

    private void updateButtonState(boolean connected) {
        if (connected) {
            connectDisconnectButton.setText("断开");
            connectDisconnectButton.setEnabled(true); // 根据需要启用或禁用按钮
        } else {
            connectDisconnectButton.setText("连接");
            connectDisconnectButton.setEnabled(true); // 根据需要启用或禁用按钮
        }
    }
    private void showServerConfigDialog() {
        ServerConfigDialog serverConfigDialog = new ServerConfigDialog(MainForm.this);
        serverConfigDialog.setVisible(true);
    }
    private void showConfigDialog() {
        ConfigDialog configDialog = new ConfigDialog(MainForm.this);
        configDialog.setVisible(true);
    }

    private void sendMessage() {

        // 发送消息逻辑
        String message = messageTextField.getText();

            chatBox.append("我 :" + message + "\n");


        messageTextField.setText(""); // 清空输入框
    }
    public void broadcastToChatBox(String message) {
        // 假设聊天框是多线程安全的，如果不是，你可能需要同步这个操作
        chatBox.append("广播: " + message + "\n");
    }

    class ServerConfigDialog extends JDialog {
        private JTextField maxUsersTextField;
        private JTextField maxbroadcastTextField;
        private JTextField portTextField;
        private JButton startStopButton; // 启动/停止按钮
        private JButton broadcastButton;//广播按钮
        private JButton okButton;
        private JButton cancelButton;
        private boolean isRunning = false; // 跟踪服务是否正在运行
        public ServerConfigDialog(Frame owner) {
            super(owner, "服务器配置", true);
            initComponents();
            pack();
            setLocationRelativeTo(owner);
        }

        private void initComponents() {
            // 人数上限输入
            JLabel maxUsersLabel = new JLabel("人数上限:");
            maxUsersTextField = new JTextField(5);
            maxbroadcastTextField=new JTextField(30);

            // 端口号输入
            JLabel portLabel = new JLabel("端口号:");
            portTextField = new JTextField(5);

            startStopButton = new JButton("启动");

            startStopButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!isRunning) {
                        startServer();
                    } else {
                        stopServer();
                    }
                }
            });
            // 添加广播按钮
            broadcastButton = new JButton("广播");
            broadcastButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    broadcastMessage();
                }
            });

            // 确定和取消按钮
            okButton = new JButton("确定");
            cancelButton = new JButton("取消");

            // 按钮事件
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onOk();
                }
            });
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });

            // 布局
            setLayout(new FlowLayout());
            add(maxUsersLabel);
            add(maxUsersTextField);
            add(portLabel);
            add(portTextField);
            add(startStopButton);
            add(maxbroadcastTextField);
            add(broadcastButton);
            add(okButton);
            add(cancelButton);
        }
        private void startServer() {
            // 启动服务器逻辑
            isRunning = true;
            updateStartStopButton(true);
            // 这里可以添加代码来启动服务器
            chatBox.append("服务器启动，端口号: " + portTextField.getText()+"\n");


        }

        private void stopServer() {
            // 停止服务器逻辑
            isRunning = false;
            updateStartStopButton(false);
            // 这里可以添加代码来停止服务器
            chatBox.append("服务器停止\n");
        }

        private void updateStartStopButton(boolean running) {
            if (running) {
                startStopButton.setText("停止");
                // 根据需要启用或禁用其他组件
            } else {
                startStopButton.setText("启动");
                // 根据需要启用或禁用其他组件
            }
        }
        private void onOk() {
            // 获取输入值
            String maxUsers = maxUsersTextField.getText();
            String port = portTextField.getText();

            // 这里可以添加代码来处理输入值，例如保存配置或更新UI
            // 示例代码，实际应用中可能需要将配置保存到某个地方或应用到程序中
            System.out.println("人数上限: " + maxUsers);
            System.out.println("端口号: " + port);

            // 关闭对话框
            dispose();
        }
        private void broadcastMessage() {
            // 假设我们有一个方法来获取要广播的消息内容
            String broadcastContent = maxbroadcastTextField.getText();

            // 调用广播消息的方法，这个方法需要你在ClientUI类中实现
            // 这个方法将会把广播内容显示在聊天框中
            broadcastToChatBox(broadcastContent);
        }
    }
    public class ConfigDialog extends JDialog {

        private JTextField nameTextField;
        private JTextField serverIpTextField;
        private JTextField portTextField;
        private JButton okButton;
        private JButton cancelButton;

        public ConfigDialog(JFrame owner) {
            super(owner, "配置", true);
            initComponents();
            pack();
            setLocationRelativeTo(owner);
        }
        private void initComponents() {
            // 姓名输入
            JLabel nameLabel = new JLabel("姓名:");
            nameTextField = new JTextField(10);


            // 服务器IP输入
            JLabel serverIpLabel = new JLabel("服务器IP:");
            serverIpTextField = new JTextField(15);

            // 端口号输入
            JLabel portLabel = new JLabel("端口号:");
            portTextField = new JTextField(5);

            // 确定和取消按钮
            okButton = new JButton("确定");
            cancelButton = new JButton("取消");


            // 按钮事件
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onOk();
                }
            });
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose(); // 关闭对话框
                }
            });

            // 布局
            setLayout(new FlowLayout());

            add(nameLabel);
            add(nameTextField);
            add(serverIpLabel);
            add(serverIpTextField);
            add(portLabel);
            add(portTextField);
            add(okButton);
            add(cancelButton);
        }

        private void onOk() {
            // 获取输入值
            String name = nameTextField.getText();
            String serverIp = serverIpTextField.getText();
            String port = portTextField.getText();

            // 这里可以添加代码来处理输入值，例如保存配置或更新UI

            // 关闭对话框
            dispose();
        }
}

        public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainForm();
            }
        });
    }
}