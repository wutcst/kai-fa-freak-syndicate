package cn.edu.whut.sept.zuul.graphics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Room {
    private String name;
    private Color backgroundColor;
    private int level;
    private List<Item> items;

    public Room(String name, Color bgColor, int levelNum) {
        this.name = name;
        this.backgroundColor = bgColor;
        this.level = levelNum;
        items = new ArrayList<>();
    }

    // Getter方法，解决找不到符号
    public String getName() {
        return name;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public int getLevel() {
        return level;
    }

    public List<Item> getItems() {
        return items;
    }

    public void addItem(Item item) {
        items.add(item);
    }
}