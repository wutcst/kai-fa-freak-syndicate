package cn.edu.whut.sept.zuul.graphics;

import javax.swing.*;

public class GameFrame extends JFrame {

    public GameFrame() {
        setTitle("探险游戏");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);          // 允许调整大小
        setExtendedState(JFrame.MAXIMIZED_BOTH); // 启动时全屏（可选）
        // 或者设置固定大尺寸：
        // setSize(1280, 720);
        // setLocationRelativeTo(null);

        GamePanel panel = new GamePanel();
        add(panel);

        setVisible(true);
    }

    public static void main(String[] args) {
        new GameFrame();
    }
}