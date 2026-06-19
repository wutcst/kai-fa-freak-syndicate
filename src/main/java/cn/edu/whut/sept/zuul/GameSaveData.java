package cn.edu.whut.sept.zuul;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.edu.whut.sept.zuul.graphics.Item;

/**
 * 游戏存档数据对象 - 可序列化
 * 用于保存和加载玩家游戏进度
 */
public class GameSaveData implements Serializable {
    private static final long serialVersionUID = 2L;

    // 游戏进度
    public int currentRoomIndex;
    public int round;
    public int score;
    public int health;
    public int maxHealth;
    public boolean hasKey;
    public int playerX;
    public int playerY;

    // 背包数据
    public int maxWeight;
    public int currentWeight;
    public List<Item> inventoryItems;

    public GameSaveData() {
        inventoryItems = new ArrayList<>();
    }
}