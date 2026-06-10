package cn.edu.whut.sept.zuul.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private Timer timer;
    private int playerX = 400, playerY = 500;
    private int playerWidth = 40, playerHeight = 40;
    private int speed = 5;

    private boolean leftPressed, rightPressed, upPressed, downPressed;

    // 物品系统相关变量
    private List<Item> items;
    private int score;

    // 图片变量
    private BufferedImage playerImage;
    private BufferedImage backgroundImage;
    private BufferedImage itemImage;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        // 初始化物品和分数
        items = new ArrayList<>();
        score = 0;
        initItems();

        // 加载玩家图片
        try {
            playerImage = ImageIO.read(getClass().getResource("/player.png"));
            if (playerImage == null) {
                System.out.println("警告：找不到图片 /player.png，将使用红色方块代替");
            }
        } catch (Exception e) {
            System.out.println("玩家图片加载失败：" + e.getMessage());
        }

        // 加载背景图片
        try {
            backgroundImage = ImageIO.read(getClass().getResource("/background.png"));
            if (backgroundImage == null) {
                System.out.println("警告：找不到图片 /background.png，将使用渐变背景代替");
            }
        } catch (Exception e) {
            System.out.println("背景图片加载失败：" + e.getMessage());
        }

        // 加载物品图片
        try {
            itemImage = ImageIO.read(getClass().getResource("/item.png"));
            if (itemImage == null) {
                System.out.println("警告：找不到图片 /item.png，将使用星星代替");
            }
        } catch (Exception e) {
            System.out.println("物品图片加载失败：" + e.getMessage());
        }

        timer = new Timer(1000 / 60, this);
        timer.start();
    }

    // 初始化地图上的物品
    private void initItems() {
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

        // 检查是否拾取物品
        checkPickup();

        repaint();
    }

    // 检查并处理物品拾取
    private void checkPickup() {
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        Item toRemove = null;

        for (Item item : items) {
            if (playerRect.intersects(item.getBounds())) {
                toRemove = item;
                score++;
                break;
            }
        }

        if (toRemove != null) {
            items.remove(toRemove);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // ========== 1. 背景图片 ==========
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, 800, 600, null);
        } else {
            // 图片加载失败时使用渐变背景
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(25, 35, 60),
                    0, 600, new Color(10, 15, 30)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, 800, 600);
        }

        // ========== 2. 网格线（可选，透明度降低）==========
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i <= 800; i += 50) {
            g2d.drawLine(i, 0, i, 600);
            g2d.drawLine(0, i, 800, i);
        }

        // ========== 3. 随机星星点缀 ==========
        g2d.setColor(new Color(255, 255, 200, 150));
        for (int i = 0; i < 100; i++) {
            int x = (i * 131) % 800;
            int y = (i * 253) % 600;
            g2d.fillOval(x, y, 2, 2);
        }

        // 画物品
        for (Item item : items) {
            drawItem(g2d, item.getX(), item.getY());
        }

        // 画玩家
        if (playerImage != null) {
            g2d.drawImage(playerImage, playerX, playerY, playerWidth, playerHeight, null);
        } else {
            g2d.setColor(Color.RED);
            g2d.fillRect(playerX, playerY, playerWidth, playerHeight);
        }

        // 画UI
        drawUI(g2d);
    }

    // 绘制物品
    private void drawItem(Graphics2D g, int x, int y) {
        if (itemImage != null) {
            g.drawImage(itemImage, x, y, 32, 32, null);
        } else {
            // 图片加载失败时使用星星
            g.setColor(new Color(255, 220, 100, 100));
            g.fillOval(x - 3, y - 3, 26, 26);
            g.setColor(new Color(255, 200, 0));
            g.fillOval(x, y, 20, 20);
            g.setColor(new Color(255, 255, 200));
            g.fillOval(x + 4, y + 4, 6, 6);
            g.setColor(Color.WHITE);
            for (int i = 0; i < 5; i++) {
                int angle = i * 72;
                int dx = (int) (Math.cos(Math.toRadians(angle)) * 12);
                int dy = (int) (Math.sin(Math.toRadians(angle)) * 12);
                g.fillOval(x + 10 + dx - 2, y + 10 + dy - 2, 3, 3);
            }
        }
    }

    // 绘制UI面板
    private void drawUI(Graphics2D g) {
        // 半透明主面板
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(10, 10, 780, 70, 25, 25);

        // 发光边框
        g.setColor(new Color(100, 180, 250, 180));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(10, 10, 780, 70, 25, 25);

        // 分数
        g.setFont(new Font("微软雅黑", Font.BOLD, 24));
        g.setColor(Color.WHITE);
        g.drawString("⭐ " + score, 30, 55);

        // 提示
        g.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        g.setColor(new Color(200, 200, 200));
        g.drawString("方向键移动  收集星星", 580, 55);
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