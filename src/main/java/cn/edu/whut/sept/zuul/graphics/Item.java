package cn.edu.whut.sept.zuul.graphics;

import java.awt.Rectangle;

public class Item {
    private String name;
    private String description;
    private int weight;
    private int x, y;
    private int width = 20, height = 20;

    // 旧构造方法（兼容现有代码）
    public Item(String name, int x, int y) {
        this(name, name, 1, x, y);
    }

    // 新构造方法
    public Item(String name, String description, int weight, int x, int y) {
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.x = x;
        this.y = y;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getWeight() { return weight; }
    public int getX() { return x; }
    public int getY() { return y; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
}