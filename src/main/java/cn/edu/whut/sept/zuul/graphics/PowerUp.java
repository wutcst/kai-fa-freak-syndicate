package cn.edu.whut.sept.zuul.graphics;

import java.awt.*;

public class PowerUp {
    public enum Type {
        SPEED,      // 加速
        INVINCIBLE, // 无敌
        DOUBLE      // 双倍分数
    }

    private int x, y;
    private Type type;
    private int size = 25;

    public PowerUp(int x, int y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void draw(Graphics2D g) {
        // 外发光
        g.setColor(new Color(255, 255, 255, 100));
        g.fillOval(x - 2, y - 2, size + 4, size + 4);

        switch (type) {
            case SPEED:
                g.setColor(new Color(100, 200, 100));
                g.fillOval(x, y, size, size);
                g.setColor(Color.WHITE);
                g.setFont(new Font("微软雅黑", Font.BOLD, 16));
                g.drawString("⚡", x + 6, y + 19);
                break;
            case INVINCIBLE:
                g.setColor(new Color(200, 100, 200));
                g.fillOval(x, y, size, size);
                g.setColor(Color.WHITE);
                g.drawString("✨", x + 6, y + 19);
                break;
            case DOUBLE:
                g.setColor(new Color(255, 200, 50));
                g.fillOval(x, y, size, size);
                g.setColor(Color.WHITE);
                g.setFont(new Font("微软雅黑", Font.BOLD, 12));
                g.drawString("2x", x + 6, y + 18);
                break;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }

    public Type getType() { return type; }
    public int getX() { return x; }
    public int getY() { return y; }
}