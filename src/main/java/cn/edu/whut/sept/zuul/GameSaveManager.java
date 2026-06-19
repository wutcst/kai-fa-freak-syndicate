package cn.edu.whut.sept.zuul;

import java.io.*;

/**
 * 游戏存档管理器 - 使用本地文件保存/加载游戏进度
 */
public class GameSaveManager {
    private static final String SAVE_DIR = System.getProperty("user.home") + File.separator + ".zuul_game";
    private static final String SAVE_PREFIX = "save_";
    private static final String SAVE_SUFFIX = ".dat";

    public static void saveGame(String username, GameSaveData data) {
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) dir.mkdirs();
        File saveFile = new File(dir, SAVE_PREFIX + username + SAVE_SUFFIX);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            oos.writeObject(data);
            System.out.println("✅ 游戏已保存: " + saveFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ 保存失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static GameSaveData loadGame(String username) {
        File saveFile = new File(SAVE_DIR, SAVE_PREFIX + username + SAVE_SUFFIX);
        if (!saveFile.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
            return (GameSaveData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ 加载存档失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static boolean hasSave(String username) {
        return new File(SAVE_DIR, SAVE_PREFIX + username + SAVE_SUFFIX).exists();
    }

    public static void deleteSave(String username) {
        File saveFile = new File(SAVE_DIR, SAVE_PREFIX + username + SAVE_SUFFIX);
        if (saveFile.exists()) {
            saveFile.delete();
            System.out.println("🗑️ 已删除旧存档: " + username);
        }
    }
}