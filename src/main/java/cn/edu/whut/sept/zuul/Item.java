package cn.edu.whut.sept.zuul;

/**
 * 物品类
 * @author 你的名字
 */
public class Item {
    private String name;
    private String description;
    private int weight;

    public Item(String name, String description, int weight) {
        this.name = name;
        this.description = description;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return name + " (" + weight + "kg) - " + description;
    }
}