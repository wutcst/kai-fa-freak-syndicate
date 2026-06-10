package cn.edu.whut.sept.zuul.graphics;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 房间/关卡类 - 存储单个房间的所有数据
 */
public class Room {
    private String name;                    // 房间名称
    private Color backgroundColor;          // 背景颜色
    private List<Item> items;               // 房间内的物品
    private Rectangle doorArea;             // 门的区域（玩家碰到进入下一关）
    private int roomLevel;                  // 房间等级（第几关）
    private int requiredItems;              // 需要收集的物品数量（可选，用于过关条件）

    public Room(String name, Color backgroundColor, int roomLevel) {
        this.name = name;
        this.backgroundColor = backgroundColor;
        this.roomLevel = roomLevel;
        this.items = new ArrayList<>();
        this.requiredItems = 0;
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

    public void setDoorArea(Rectangle doorArea) {
        this.doorArea = doorArea;
    }

    public Rectangle getDoorArea() {
        return doorArea;
    }

    public int getRoomLevel() {
        return roomLevel;
    }

    public int getRequiredItems() {
        return requiredItems;
    }

    public void setRequiredItems(int requiredItems) {
        this.requiredItems = requiredItems;
    }

    // 检查是否还有物品
    public boolean hasItemsRemaining() {
        return !items.isEmpty();
    }

    // 获取物品数量
    public int getItemCount() {
        return items.size();
    }
}