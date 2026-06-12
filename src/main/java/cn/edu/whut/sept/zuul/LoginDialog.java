package cn.edu.whut.sept.zuul;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * 登录对话框 - 按钮颜色彻底修复版
 */
public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberCheckBox;
    private JButton loginButton;
    private JButton registerButton;
    private JButton guestButton;
    private JLabel messageLabel;
    private boolean loginSuccess = false;
    private boolean isGuestMode = false;

    public LoginDialog(JFrame parent) {
        super(parent, "游戏登录", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(400, 380);
        initComponents();
        setupListeners();
        loadSavedCredentials();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 35, 45));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 30, 30));

        JLabel titleLabel = new JLabel("探险游戏");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 200, 100));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel userLabel = new JLabel("用户名：");
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        userLabel.setForeground(new Color(220, 220, 220));
        userLabel.setPreferredSize(new Dimension(60, 30));
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        usernameField = new JTextField();
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(200, 32));
        usernameField.setBackground(new Color(50, 55, 65));
        usernameField.setForeground(Color.WHITE);
        usernameField.setCaretColor(Color.WHITE);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 85, 95)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        formPanel.add(usernameField, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel passLabel = new JLabel("密码：");
        passLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passLabel.setForeground(new Color(220, 220, 220));
        formPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(200, 32));
        passwordField.setBackground(new Color(50, 55, 65));
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 85, 95)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        formPanel.add(passwordField, gbc);

        // 记住密码
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        rememberCheckBox = new JCheckBox("记住密码");
        rememberCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        rememberCheckBox.setForeground(new Color(180, 180, 180));
        rememberCheckBox.setOpaque(false);
        formPanel.add(rememberCheckBox, gbc);

        // 消息提示
        gbc.gridy = 3;
        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(255, 120, 120));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(messageLabel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setOpaque(false);
        loginButton = createStyledButton("登录", new Color(41, 128, 185));
        registerButton = createStyledButton("注册", new Color(184, 134, 11));
        guestButton = createStyledButton("游客模式", new Color(192, 57, 43));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(guestButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * 按钮样式 - 彻底修复背景色 + 鼠标悬浮/点击效果
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 22, 8, 22));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 核心修复：让背景色生效
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);

        // 鼠标交互
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(bgColor);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    private void setupListeners() {
        loginButton.addActionListener(e -> doLogin());
        registerButton.addActionListener(e -> doRegister());
        guestButton.addActionListener(e -> doGuestMode());

        KeyAdapter enterAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doLogin();
                }
            }
        };
        usernameField.addKeyListener(enterAdapter);
        passwordField.addKeyListener(enterAdapter);
    }

    private void loadSavedCredentials() {
        UserManager um = UserManager.getInstance();
        if (um.isRememberMe()) {
            String username = um.getSavedUsername();
            String password = um.getSavedPassword();
            if (username != null && !username.isEmpty()) {
                usernameField.setText(username);
                if (password != null && !password.isEmpty()) {
                    passwordField.setText(password);
                }
                rememberCheckBox.setSelected(true);
            }
        }
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty()) {
            messageLabel.setText("请输入用户名");
            return;
        }
        if (password.isEmpty()) {
            messageLabel.setText("请输入密码");
            return;
        }
        UserManager um = UserManager.getInstance();
        String error = um.login(username, password);
        if (error == null) {
            um.saveRememberMe(username, password, rememberCheckBox.isSelected());
            loginSuccess = true;
            isGuestMode = false;
            dispose();
        } else {
            messageLabel.setText(error);
            passwordField.setText("");
            passwordField.requestFocus();
            Point original = getLocation();
            Timer timer = new Timer(20, new ActionListener() {
                int count = 0;
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (count < 5) {
                        setLocation(original.x + (count % 2 == 0 ? 5 : -5), original.y);
                        count++;
                    } else {
                        setLocation(original);
                        ((Timer)e.getSource()).stop();
                    }
                }
            });
            timer.start();
        }
    }

    private void doRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty()) {
            messageLabel.setText("请输入用户名");
            return;
        }
        if (password.isEmpty()) {
            messageLabel.setText("请输入密码");
            return;
        }
        UserManager um = UserManager.getInstance();
        String error = um.register(username, password);
        if (error == null) {
            JOptionPane.showMessageDialog(this,
                    "注册成功！请使用新账号登录。",
                    "注册成功",
                    JOptionPane.INFORMATION_MESSAGE);
            passwordField.setText("");
            messageLabel.setText("");
        } else {
            messageLabel.setText(error);
        }
    }

    private void doGuestMode() {
        loginSuccess = true;
        isGuestMode = true;
        dispose();
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    public boolean isGuestMode() {
        return isGuestMode;
    }
}