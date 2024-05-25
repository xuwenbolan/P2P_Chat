package com.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ConcurrentHashMap;

import com.control.Ping;
import com.model.User;

public class MainForm {
    protected JFrame frame;
    protected JPanel messagePanel;
    protected JButton configButton;
    protected JTextArea chatBox;
    protected JSplitPane centerSplitPanel; //分隔面板
    protected JScrollPane userPanel,       //右边用户面板
            messageBoxPanel; //左边消息框
    protected JTextField messageTextField;
    protected JButton sendButton;
    protected JButton SwitchC_Button; // 合并为一个按钮

    protected boolean isConnected = false; // 跟踪连接状态
    protected JList userList;               //动态变化的用户列表
    protected DefaultListModel<String> listModel;
    protected JTextArea logTextArea;              //服务器日志

    public static String Username = "User";
    public static int Port = 9999;

    public static ConcurrentHashMap<String, User> onlineUsers = new ConcurrentHashMap<String, User>();

    public MainForm() {
        frame = new JFrame("P2P Talk");
        Toolkit kit = Toolkit.getDefaultToolkit(); // 定义工具包
        Dimension screenSize = kit.getScreenSize(); // 获取屏幕的尺寸
        int screenWidth = screenSize.width / 2; // 获取屏幕的宽
        int screenHeight = screenSize.height / 2; // 获取屏幕的高
        int width = 700;
        int hight = 500;
        frame.setBounds(screenWidth - width / 2, screenHeight - hight / 2, width, hight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        ImageIcon icon = new ImageIcon("./res/img/icon.png");
        this.frame.setIconImage(icon.getImage());

        //在线用户面板
        listModel = new DefaultListModel<String>();
        userList = new JList(listModel);
        userPanel = new JScrollPane(userList);
        userPanel.setBorder(new TitledBorder("在线用户"));  //设置在线用户面板标题
        listModel.addElement("聊天人1");
        listModel.addElement("聊天人2");
        listModel.addElement("聊天人3");

        //接收消息面板
        chatBox = new JTextArea();
        chatBox.setEditable(false);        //设置该区域不可编辑
        chatBox.setForeground(Color.blue); //设置字体默认颜色为蓝色
        messageBoxPanel = new JScrollPane(chatBox);   //设置为带滑动条的文本框
        messageBoxPanel.setBorder(new TitledBorder("接收消息")); //设置标题

        //发送消息组件
        configButton = new JButton("配置");
        configButton.setBackground(Color.white);

        messageTextField = new JTextField();
        sendButton = new JButton("发送");
        sendButton.setBackground(Color.white);
        messagePanel = new JPanel(new BorderLayout());  //将组件放置在面板上
        messagePanel.add(configButton,"West");
        messagePanel.add(messageTextField, "Center");
        messagePanel.add(sendButton, "East");
        messagePanel.setBorder(new TitledBorder("发送消息"));


        //将中间在线用户面板与接收消息面板组合起来
        centerSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, messageBoxPanel,userPanel );
        centerSplitPanel.setDividerLocation(500);  //设置分隔线离左边100px

        frame.add(centerSplitPanel, "Center");
        frame.add(messagePanel, "South");
        configButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showConfigDialog();
            }
        });

        frame.setVisible(true);
    }

    private void showConfigDialog() {
        ConfigDialog configDialog = new ConfigDialog(this.frame);
        configDialog.setVisible(true);
    }

    private void sendMessage() {

        // 发送消息逻辑
        String message = messageTextField.getText();

        chatBox.append("我 :" + message + "\n");


        messageTextField.setText(""); // 清空输入框
    }

    public class ConfigDialog extends JDialog {

        public JTextField nameTextField;
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
            JLabel nameLabel = new JLabel("用户名:");
            nameTextField = new JTextField(10);
            nameTextField.setText(Username);
//            nameTextField.setForeground(new Color(170, 170, 170)); // 浅灰色

            // 端口号输入
            JLabel portLabel = new JLabel("端口号:");
            portTextField = new JTextField(5);
            portTextField.setDocument(new NumberDocument());
            portTextField.setText(Integer.toString(Port));
//            portTextField.setForeground(new Color(170, 170, 170)); // 浅灰色

            // 确定和取消按钮
            okButton = new JButton("确定");
            okButton.setBackground(Color.white);
            cancelButton = new JButton("取消");
            cancelButton.setBackground(Color.white);


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
            add(portLabel);
            add(portTextField);
            add(okButton);
            add(cancelButton);
        }

        private void onOk() {
            // 获取输入值
            String nameInput = nameTextField.getText().trim();
            String portInput = portTextField.getText().trim();

            // 检查输入是否为空
            if (nameInput.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "用户名不能为空。", "输入错误", JOptionPane.ERROR_MESSAGE);
                return; // 退出onOk方法，不关闭对话框
            }
            if (portInput.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "端口号不能为空。", "输入错误", JOptionPane.ERROR_MESSAGE);
                return; // 退出onOk方法，不关闭对话框
            }

            try {
                // 尝试将端口号文本转换为整数
                int portNumber = Integer.parseInt(portInput);
                // 检查端口号是否在有效的范围内（例如0-65535）
                if (portNumber < 0 || portNumber > 65535) {
                    JOptionPane.showMessageDialog(this, "端口号必须在0到65535之间。", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return; // 退出onOk方法，不关闭对话框
                }

                // 如果输入有效，更新Username和Port变量
                Username = nameInput;
                Port = portNumber;

                // ... 可以添加其他处理输入值的代码 ...

                // 关闭对话框
                dispose();
            } catch (NumberFormatException e) {
                // 端口号文本不是有效的整数
                JOptionPane.showMessageDialog(this, "端口号必须为有效的整数。", "输入错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    public class NumberDocument extends PlainDocument {
        public NumberDocument() {
        }

        public void insertString(int var1, String var2, AttributeSet var3) throws BadLocationException {
            if (this.isNumeric(var2)) {
                super.insertString(var1, var2, var3);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }

        }

        private boolean isNumeric(String var1) {
            try {
                Long.valueOf(var1);
                return true;
            } catch (NumberFormatException var3) {
                return false;
            }
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