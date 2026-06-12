package cn.edu.whut.sept.zuul.graphics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Room {
    private String name;
    private Color backgroundColor;
    private int level;
    private List<Item> items;  // 已经是List，天然支持重复物品

    public Room(String name, Color bgColor, int levelNum) {
        this.name = name;
        this.backgroundColor = bgColor;
        this.level = levelNum;
        items = new ArrayList<>();
    }

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

    // 清空物品（用于重新生成）
    public void clearItems() {
        items.clear();
    }
}