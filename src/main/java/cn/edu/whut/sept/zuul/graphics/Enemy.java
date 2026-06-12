package cn.edu.whut.sept.zuul.graphics;

import java.awt.*;

public class Enemy {
    private int x, y;
    private int width = 35, height = 35;
    private double speed;  // 敌人自己的固定速度
    private String name;
    private Color normalColor;
    private Color stunnedColor;
    private boolean isStunned;
    private int stunTimer;

    // 玩家最大速度常量（5）
    private static final int PLAYER_MAX_SPEED = 5;

    public Enemy(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.name = "红色追击者";
        this.normalColor = new Color(200, 50, 50);
        this.stunnedColor = new Color(100, 100, 200);
        // 敌人速度 = 玩家最大速度的0.7倍
        this.speed = PLAYER_MAX_SPEED * 0.7;  // 5 * 0.7 = 3.5
        this.isStunned = false;
        this.stunTimer = 0;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public double getSpeed() { return speed; }
    public boolean isStunned() { return isStunned; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void stun(int duration) {
        this.isStunned = true;
        this.stunTimer = duration;
    }

    public void updateStun() {
        if (isStunned) {
            stunTimer--;
            if (stunTimer <= 0) {
                isStunned = false;
                stunTimer = 0;
            }
        }
    }

    /**
     * 向玩家方向移动（使用自己的固定速度）
     */
    public void moveTowardsPlayer(int playerX, int playerY) {
        if (isStunned) return;

        double dx = playerX - x;
        double dy = playerY - y;

        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            double moveX = (dx / distance) * speed;
            double moveY = (dy / distance) * speed;

            x += (int) Math.round(moveX);
            y += (int) Math.round(moveY);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void draw(Graphics2D g) {
        if (isStunned) {
            g.setColor(stunnedColor);
        } else {
            g.setColor(normalColor);
        }

        g.fillRoundRect(x, y, width, height, 8, 8);

        if (isStunned) {
            g.setColor(new Color(255, 255, 100));
            g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            g.drawString("⭐", x - 8, y + 10);
            g.drawString("⭐", x + width + 2, y + 10);
            g.drawString("⭐", x + width/2 - 9, y - 8);
        }

        g.setColor(Color.WHITE);
        g.fillOval(x + 8, y + 10, 8, 8);
        g.fillOval(x + width - 16, y + 10, 8, 8);

        if (isStunned) {
            g.setColor(new Color(80, 80, 120));
            g.drawOval(x + 9, y + 12, 5, 5);
            g.drawOval(x + width - 15, y + 12, 5, 5);

            g.setColor(new Color(180, 180, 255));
            g.setFont(new Font("微软雅黑", Font.BOLD, 10));
            g.drawString("Z", x + width - 8, y - 5);
            g.drawString("Z", x + width - 14, y - 10);
            g.drawString("Z", x + width - 20, y - 15);
        } else {
            g.setColor(Color.BLACK);
            g.fillOval(x + 10, y + 12, 4, 4);
            g.fillOval(x + width - 14, y + 12, 4, 4);

            g.setStroke(new BasicStroke(2));
            g.drawLine(x + 6, y + 6, x + 12, y + 8);
            g.drawLine(x + width - 12, y + 6, x + width - 6, y + 8);

            g.setColor(Color.BLACK);
            g.setFont(new Font("微软雅黑", Font.BOLD, 12));
            g.drawString("危", x + width/2 - 6, y + height - 8);
        }
    }
}