package cn.edu.whut.sept.zuul.graphics;

import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.*;

/** 暂停控制器 - 处理暂停、丢弃物品 */
public class PauseController {
  private boolean paused = false;
  private GamePanel gamePanel; // 用于获取玩家引用和显示消息

  public PauseController(GamePanel panel) {
    this.gamePanel = panel;
  }

  public boolean isPaused() {
    return paused;
  }

  public void togglePause() {
    paused = !paused;
    if (paused) {
      gamePanel.showMessage("⏸ 游戏暂停，按 P 恢复 | 按 I 查看背包，按数字键丢弃物品", 120);
    } else {
      gamePanel.showMessage("▶ 游戏继续", 60);
    }
    gamePanel.repaint();
  }

  /**
   * 在暂停状态下处理按键（数字键丢弃物品）
   *
   * @param keyCode 键码
   * @return true 表示已处理，false 表示未处理
   */
  public boolean handlePausedKey(int keyCode) {
    if (!paused) return false;

    // 数字键 1~9
    if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_9) {
      int index = keyCode - KeyEvent.VK_1; // 0-based
      List<Item> inventory = gamePanel.getPlayer().getInventory();
      if (index < inventory.size()) {
        Item item = inventory.get(index);
        // 丢弃物品
        gamePanel.getPlayer().dropItem(item);
        gamePanel.showMessage("丢弃 " + item.getName() + "（-" + item.getWeight() + "kg）", 60);
        // 丢弃后立即重新计算速度（可能会提速）
        gamePanel.updateSpeedByWeight();
        // 刷新UI
        gamePanel.repaintSidePanels();
      } else {
        gamePanel.showMessage("无效的数字，背包没有第 " + (index + 1) + " 个物品", 40);
      }
      return true;
    }
    return false;
  }
}
