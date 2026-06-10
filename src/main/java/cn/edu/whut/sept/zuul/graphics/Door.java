package cn.edu.whut.sept.zuul.graphics;

import java.awt.*;

/**
 * 门/传送门类 - 玩家碰到后切换到下一房间
 */
public class Door {
    private int x, y;
    private int width, height;
    private Color color;
    private boolean isActive;

    public Door(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = new Color(100, 200, 255, 150);
        this.isActive = true;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void draw(Graphics2D g) {
        if (!isActive) return;

        // 绘制发光门效果
        g.setColor(color);
        g.fillRect(x, y, width, height);

        // 边框
        g.setColor(new Color(255, 255, 255, 200));
        g.setStroke(new BasicStroke(3));
        g.drawRect(x, y, width, height);

        // 门文字
        g.setFont(new Font("微软雅黑", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("→ 下一关 →", x + 15, y + 30);
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isActive() {
        return isActive;
    }
}
