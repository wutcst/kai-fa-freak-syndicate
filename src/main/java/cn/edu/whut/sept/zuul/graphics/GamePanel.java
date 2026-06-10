package cn.edu.whut.sept.zuul.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private Timer timer;
    private int playerX = 400, playerY = 500;
    private int playerWidth = 40, playerHeight = 40;
    private int speed = 5;

    private boolean leftPressed, rightPressed, upPressed, downPressed;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        timer = new Timer(1000 / 60, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 移动玩家
        if (leftPressed) playerX -= speed;
        if (rightPressed) playerX += speed;
        if (upPressed) playerY -= speed;
        if (downPressed) playerY += speed;

        // 边界限制
        if (playerX < 0) playerX = 0;
        if (playerX > 760) playerX = 760;
        if (playerY < 0) playerY = 0;
        if (playerY > 560) playerY = 560;

        repaint(); // 重绘画面
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 画玩家（红色方块）
        g.setColor(Color.RED);
        g.fillRect(playerX, playerY, playerWidth, playerHeight);

        // 显示提示文字
        g.setColor(Color.WHITE);
        g.drawString("使用方向键移动红色方块", 10, 20);
    }

    // 键盘按下
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) leftPressed = true;
        if (key == KeyEvent.VK_RIGHT) rightPressed = true;
        if (key == KeyEvent.VK_UP) upPressed = true;
        if (key == KeyEvent.VK_DOWN) downPressed = true;
    }

    // 键盘松开
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) leftPressed = false;
        if (key == KeyEvent.VK_RIGHT) rightPressed = false;
        if (key == KeyEvent.VK_UP) upPressed = false;
        if (key == KeyEvent.VK_DOWN) downPressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}