package cn.edu.whut.sept.zuul.graphics;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private int x, y;
    private int width = 35, height = 35;
    private int speed = 5;
    private int originalSpeed = 5;

    // 背包和负重系统
    private List<Item> inventory;
    private int maxWeight = 20;
    private int currentWeight = 0;

    // 玩家基本信息
    private String name;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.name = "冒险者";
        this.inventory = new ArrayList<>();
    }

    // ========== 移动相关 ==========
    public void update(boolean left, boolean right, boolean up, boolean down) {
        if (left) x -= speed;
        if (right) x += speed;
        if (up) y -= speed;
        if (down) y += speed;
        if (x < 0) x = 0;
        if (x > 765) x = 765;
        if (y < 0) y = 0;
        if (y > 565) y = 565;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void resetSpeed() {
        this.speed = originalSpeed;
    }

    public int getSpeed() {
        return speed;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // ========== 玩家信息 ==========
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // ========== 背包和负重系统 ==========
    public List<Item> getInventory() {
        return inventory;
    }

    public int getMaxWeight() {
        return maxWeight;
    }

    public int getCurrentWeight() {
        return currentWeight;
    }

    public void increaseMaxWeight(int amount) {
        this.maxWeight += amount;
    }

    public boolean takeItem(Item item) {
        if (currentWeight + item.getWeight() <= maxWeight) {
            inventory.add(item);
            currentWeight += item.getWeight();
            return true;
        }
        return false;
    }

    public void dropItem(Item item) {
        if (inventory.remove(item)) {
            currentWeight -= item.getWeight();
        }
    }

    public void dropAllItems() {
        inventory.clear();
        currentWeight = 0;
    }

    public String getInventoryDescription() {
        if (inventory.isEmpty()) {
            return "背包是空的。";
        }
        StringBuilder sb = new StringBuilder("你的背包里有：\n");
        for (Item item : inventory) {
            sb.append("  - ").append(item.getName()).append(" (").append(item.getWeight())
                    .append("kg): ").append(item.getDescription()).append("\n");
        }
        sb.append("总重量：").append(currentWeight).append("/").append(maxWeight).append("kg");
        return sb.toString();
    }
}