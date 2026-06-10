package cn.edu.whut.sept.zuul.graphics;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Room {
    private String name;
    private Color backgroundColor;
    private List<Item> items;
    private List<Enemy> enemies;
    private int roomLevel;

    public Room(String name, Color backgroundColor, int roomLevel) {
        this.name = name;
        this.backgroundColor = backgroundColor;
        this.roomLevel = roomLevel;
        this.items = new ArrayList<>();
        this.enemies = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public List<Item> getItems() {
        return items;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    public int getRoomLevel() {
        return roomLevel;
    }

    public int getItemCount() {
        return items.size();
    }

    public int getEnemyCount() {
        return enemies.size();
    }
}