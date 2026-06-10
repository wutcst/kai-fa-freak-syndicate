package cn.edu.whut.sept.zuul.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private Timer timer;
    private int playerX = 400, playerY = 500;
    private int playerWidth = 40, playerHeight = 40;
    private int speed = 5;

    private boolean leftPressed, rightPressed, upPressed, downPressed;

    // --- 新增：物品系统相关变量 ---
    private List<Item> items; // 存储所有物品的列表
    private int score;        // 玩家的分数

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        // --- 新增：初始化物品和分数 ---
        items = new ArrayList<>();
        score = 0;
        initItems();

        timer = new Timer(1000 / 60, this);
        timer.start();
    }

    // --- 新增：初始化地图上的物品 ---
    private void initItems() {
        // 在地图不同位置放置5个物品
        items.add(new Item("金色徽章", 100, 200));
        items.add(new Item("魔法水晶", 300, 100));
        items.add(new Item("古老卷轴", 600, 400));
        items.add(new Item("生命药水", 150, 500));
        items.add(new Item("神秘钥匙", 650, 150));
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

        // --- 新增：移动后检查是否拾取物品 ---
        checkPickup();

        repaint();
    }

    // --- 新增：检查并处理物品拾取 ---
    private void checkPickup() {
        // 获取玩家边界矩形
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        // 需要被移除的物品
        Item toRemove = null;

        for (Item item : items) {
            if (playerRect.intersects(item.getBounds())) {
                toRemove = item;
                score++; // 拾取一个，分数加1
                break;   // 一次移动只拾取一个物品
            }
        }

        // 移除被拾取的物品
        if (toRemove != null) {
            items.remove(toRemove);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 画玩家（红色方块）
        g.setColor(Color.RED);
        g.fillRect(playerX, playerY, playerWidth, playerHeight);

        // --- 新增：画所有物品（金色圆形）---
        for (Item item : items) {
            g.setColor(Color.YELLOW);
            g.fillOval(item.getX(), item.getY(), 20, 20);
            // 可选：给物品加个边框
            g.setColor(Color.ORANGE);
            g.drawOval(item.getX(), item.getY(), 20, 20);
        }

        // --- 新增：显示分数 ---
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.BOLD, 16));
        g.drawString("分数: " + score, 10, 30);
        g.drawString("目标: 收集所有金色物品", 10, 55);

        // 原有提示文字
        g.drawString("使用方向键移动红色方块", 10, 80);
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