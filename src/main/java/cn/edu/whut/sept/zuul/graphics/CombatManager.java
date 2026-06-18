package cn.edu.whut.sept.zuul.graphics;

import java.util.List;

/**
 * 战斗管理器 - 处理武器/盔甲与敌人的互动
 */
public class CombatManager {
    private GamePanel gamePanel;
    private static final int ARMOR_SPEED_BOOST_DURATION = 120;  // 2秒 (60fps * 2)
    private static final int ARMOR_SPEED_BOOST_AMOUNT = 3;     // 加速值

    public CombatManager(GamePanel panel) {
        this.gamePanel = panel;
    }

    /**
     * 处理玩家与敌人的碰撞
     * @param enemy 碰撞的敌人
     * @return true 表示敌人已死亡（或玩家死亡），false 表示未处理完
     */
    public boolean handleCollision(Enemy enemy) {
        Player player = gamePanel.getPlayer();

        // 1. 优先查找武器
        Weapon weapon = findWeaponInInventory(player.getInventory());
        if (weapon != null) {
            player.dropItem(weapon);
            gamePanel.getEnemies().remove(enemy);
            gamePanel.showMessage("⚔️ 使用 " + weapon.getName() + " 击杀了敌人！", 60);
            return true;
        }

        // 2. 查找盔甲
        Armor armor = findArmorInInventory(player.getInventory());
        if (armor != null) {
            // 消耗盔甲
            player.dropItem(armor);
            gamePanel.showMessage("🛡️ 盔甲抵挡了攻击！盔甲破碎，获得短暂加速", 60);
            gamePanel.applyTemporarySpeedBoost(ARMOR_SPEED_BOOST_DURATION, ARMOR_SPEED_BOOST_AMOUNT);

            // ========== 关键：弹开玩家和敌人，防止重复碰撞 ==========
            gamePanel.repelPlayerAndEnemy(enemy);

            return false;  // 敌人未死，但玩家已弹开
        }

        // 3. 无武器无盔甲 -> 游戏结束
        gamePanel.gameOverByEnemy();
        return false;  // 游戏结束
    }

    private Weapon findWeaponInInventory(List<Item> inventory) {
        for (Item item : inventory) {
            if (item instanceof Weapon) {
                return (Weapon) item;
            }
        }
        return null;
    }

    private Armor findArmorInInventory(List<Item> inventory) {
        for (Item item : inventory) {
            if (item instanceof Armor) {
                return (Armor) item;
            }
        }
        return null;
    }
}