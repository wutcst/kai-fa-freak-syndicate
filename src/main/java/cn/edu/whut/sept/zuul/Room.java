package cn.edu.whut.sept.zuul;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 房间类 - 游戏中的一个房间
 * @author 原作者 + 你的名字（扩充）
 */
public class Room {
    private String description;
    private Map<String, Room> exits;
    private Map<String, Item> items;

    /**
     * 构造方法
     * @param description 房间描述
     */
    public Room(String description) {
        this.description = description;
        exits = new HashMap<>();
        items = new HashMap<>();
    }

    /**
     * 设置出口
     * @param direction 方向
     * @param neighbor 相邻房间
     */
    public void setExit(String direction, Room neighbor) {
        exits.put(direction, neighbor);
    }

    /**
     * 获取简短描述
     * @return 房间描述
     */
    public String getShortDescription() {
        return description;
    }

    /**
     * 获取详细描述（包含出口和物品）
     * @return 完整描述
     */
    public String getLongDescription() {
        return "你在 " + description + "\n" + getExitString() + "\n" + getItemsDescription();
    }

    /**
     * 获取出口描述
     * @return 出口列表字符串
     */
    private String getExitString() {
        String returnString = "出口:";
        Set<String> keys = exits.keySet();
        for (String exit : keys) {
            returnString += " " + exit;
        }
        return returnString;
    }

    /**
     * 根据方向获取房间
     * @param direction 方向
     * @return 相邻房间，不存在返回null
     */
    public Room getExit(String direction) {
        return exits.get(direction);
    }

    /**
     * 添加物品到房间
     * @param item 要添加的物品
     */
    public void addItem(Item item) {
        items.put(item.getName(), item);
    }

    /**
     * 从房间获取物品
     * @param name 物品名称
     * @return 物品对象，不存在返回null
     */
    public Item getItem(String name) {
        return items.get(name);
    }

    /**
     * 从房间移除物品
     * @param name 物品名称
     */
    public void removeItem(String name) {
        items.remove(name);
    }

    /**
     * 获取房间所有物品
     * @return 物品Map
     */
    public Map<String, Item> getItems() {
        return items;
    }

    /**
     * 获取房间物品描述
     * @return 物品描述字符串
     */
    public String getItemsDescription() {
        if (items.isEmpty()) {
            return "房间里没有物品。";
        }
        StringBuilder sb = new StringBuilder("房间里的物品：");
        int totalWeight = 0;
        for (Item item : items.values()) {
            sb.append("\n  - ").append(item.toString());
            totalWeight += item.getWeight();
        }
        sb.append("\n总重量：").append(totalWeight).append("kg");
        return sb.toString();
    }
}