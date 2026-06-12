package cn.edu.whut.sept.zuul.graphics;

import javax.swing.*;
import cn.edu.whut.sept.zuul.LoginDialog;

public class GameFrame extends JFrame {
    public GameFrame() {
        setTitle("探险游戏");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // 先显示登录界面
        LoginDialog loginDialog = new LoginDialog(this);
        if (!loginDialog.isLoginSuccess()) {
            System.exit(0);
            return;
        }

        GamePanel panel = new GamePanel(loginDialog.isGuestMode());
        add(panel);
        setVisible(true);
        panel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        // 修复Swing原生样式问题，同时美化全局控件（解决按钮、窗体老旧样式）
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new GameFrame();
        });
    }
}