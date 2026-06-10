package cn.edu.whut.sept.zuul.graphics;

import java.awt.Rectangle;

public class Item {
    private String name;
    private int x, y;
    private int width = 20, height = 20; // 物品大小

    public Item(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // 获取物品的边界矩形，用于碰撞检测
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
