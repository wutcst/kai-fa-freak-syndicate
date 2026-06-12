package cn.edu.whut.sept.zuul.graphics;

import java.awt.*;

public class Door {
    protected int x, y, w, h;   // 改为 protected，允许子类访问
    private String direction;
    private int dirIndex;

    public Door(int x, int y, int w, int h, String direction) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.direction = direction;
        switch(direction) {
            case "north": dirIndex = 0; break;
            case "east":  dirIndex = 1; break;
            case "south": dirIndex = 2; break;
            case "west":  dirIndex = 3; break;
            default: dirIndex = 1; break;

        }
    }



    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }


    public void draw(Graphics2D g) {
        g.setColor(new Color(100, 200, 255, 180));
        g.fillRect(x, y, w, h);
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.BOLD, 14));
        String arrow = "→";
        if (direction.equals("north")) arrow = "↑";
        if (direction.equals("south")) arrow = "↓";
        if (direction.equals("west")) arrow = "←";
        FontMetrics fm = g.getFontMetrics();
        int arrowWidth = fm.stringWidth(arrow);
        g.drawString(arrow, x + w/2 - arrowWidth/2, y + h/2 + 5);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, w, h);
    }

    public int getDirectionIndex() {
        return dirIndex;
    }

    public String getDirection() {
        return direction;
    }
}