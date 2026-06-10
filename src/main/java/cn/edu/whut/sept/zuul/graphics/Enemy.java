package cn.edu.whut.sept.zuul.graphics;

import java.awt.*;
import java.util.Random;

/**
 * 敌人类 - 在房间内移动，碰到玩家会扣分
 */
public class Enemy {
    private int x, y;
    private int width = 35, height = 35;
    private int dx, dy;  // 移动速度方向
    private int speed = 2;
    private Color color;
    private Random random;

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
        this.random = new Random();
        // 随机初始方向
        this.dx = (random.nextBoolean() ? 1 : -1) * speed;
        this.dy = (random.nextBoolean() ? 1 : -1) * speed;
        this.color = new Color(200, 50, 50);  // 暗红色
    }

    /**
     * 带速度参数的构造方法（用于控制难度）
     */
    public Enemy(int x, int y, int speed) {
        this(x, y);
        this.speed = speed;
        this.dx = (random.nextBoolean() ? 1 : -1) * speed;
        this.dy = (random.nextBoolean() ? 1 : -1) * speed;
    }

    public void move() {
        x += dx;
        y += dy;
    }

    public void bounceIfNeeded(int panelWidth, int panelHeight) {
        // 边界反弹（考虑敌人大小）
        if (x < 0) {
            x = 0;
            dx = -dx;
        }
        if (x > panelWidth - width) {
            x = panelWidth - width;
            dx = -dx;
        }
        if (y < 0) {
            y = 0;
            dy = -dy;
        }
        if (y > panelHeight - height) {
            y = panelHeight - height;
            dy = -dy;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void draw(Graphics2D g) {
        // 绘制敌人（红色圆形 + 邪恶眼睛）
        g.setColor(color);
        g.fillOval(x, y, width, height);

        // 眼睛（白色底）
        g.setColor(Color.WHITE);
        g.fillOval(x + 8, y + 10, 8, 8);
        g.fillOval(x + 19, y + 10, 8, 8);

        // 瞳孔（黑色）
        g.setColor(Color.BLACK);
        g.fillOval(x + 10, y + 12, 4, 4);
        g.fillOval(x + 21, y + 12, 4, 4);

        // 眉毛（凶狠）
        g.setColor(Color.BLACK);
        g.drawLine(x + 6, y + 6, x + 12, y + 8);
        g.drawLine(x + 23, y + 6, x + 29, y + 8);

        // 嘴巴
        g.setColor(new Color(80, 0, 0));
        g.fillArc(x + 10, y + 22, 15, 10, 180, 180);
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
