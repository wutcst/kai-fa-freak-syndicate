package cn.edu.whut.sept.zuul;

import java.util.ArrayList;
import java.util.List;

/**
 * 玩家类 - 保存玩家信息和随身物品
 * @author 你的名字
 */
public class Player {
    private String name;
    private Room currentRoom;
    private List<Item> inventory;
    private int maxWeight;
    private int currentWeight;

    public Player(String name, Room startRoom) {
        this.name = name;
        this.currentRoom = startRoom;
        this.inventory = new ArrayList<>();
        this.maxWeight = 20;
        this.currentWeight = 0;
    }

    public String getName() {
        return name;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public int getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(int maxWeight) {
        this.maxWeight = maxWeight;
    }

    public int getCurrentWeight() {
        return currentWeight;
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
}