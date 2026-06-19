package cn.edu.whut.sept.zuul.graphics;

import cn.edu.whut.sept.zuul.GameSaveData;
import cn.edu.whut.sept.zuul.GameSaveManager;
import cn.edu.whut.sept.zuul.LoginDialog;
import cn.edu.whut.sept.zuul.UserManager;
import javax.swing.*;

public class GameFrame extends JFrame {
  public GameFrame() {
    setTitle("world of zuul探险游戏");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(true);
    setExtendedState(JFrame.MAXIMIZED_BOTH);

    // 先显示登录界面
    LoginDialog loginDialog = new LoginDialog(this);
    if (!loginDialog.isLoginSuccess()) {
      System.exit(0);
      return;
    }

    // ========== 新增：检查是否存在存档 ==========
    GameSaveData saveData = null;
    if (!loginDialog.isGuestMode()) {
      String username = UserManager.getInstance().getCurrentUser().getUsername();
      if (GameSaveManager.hasSave(username)) {
        int option =
            JOptionPane.showConfirmDialog(
                this,
                "检测到之前的游戏进度，是否继续？\n选择「是」继续游戏，选择「否」开始新游戏。",
                "继续游戏",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
          saveData = GameSaveManager.loadGame(username);
        } else {
          GameSaveManager.deleteSave(username); // 删除旧存档
        }
      }
    }
    // ===========================================

    // 创建游戏面板，传入存档数据（游客模式传入 null）
    GamePanel panel = new GamePanel(loginDialog.isGuestMode(), saveData);
    add(panel);
    setVisible(true);
    panel.requestFocusInWindow();
  }

  public static void main(String[] args) {
    // 修复Swing原生样式问题，同时美化全局控件
    try {
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }

    SwingUtilities.invokeLater(
        () -> {
          new GameFrame();
        });
  }
}
