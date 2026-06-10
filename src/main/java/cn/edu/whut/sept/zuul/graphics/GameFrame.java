package cn.edu.whut.sept.zuul.graphics;

import javax.swing.*;

public class GameFrame extends JFrame {

    public GameFrame() {
        setTitle("探险游戏");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        GamePanel panel = new GamePanel();
        add(panel);

        setVisible(true);
    }

    public static void main(String[] args) {
        new GameFrame();
    }
}