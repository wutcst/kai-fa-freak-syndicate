package cn.edu.whut.sept.zuul;

import java.io.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.prefs.Preferences;

/**
 * 用户管理器 - 处理用户注册、登录和数据持久化
 */
public class UserManager {
    private static UserManager instance;
    private Map<String, User> users;
    private User currentUser;
    private File dataFile;

    // 记住密码的存储
    private Preferences prefs;
    private static final String REMEMBER_USER = "remember_user";
    private static final String SAVED_USERNAME = "saved_username";
    private static final String SAVED_PASSWORD = "saved_password";

    private UserManager() {
        users = new HashMap<>();

        // 获取用户数据文件路径
        String userHome = System.getProperty("user.home");
        File gameDir = new File(userHome, ".zuul_game");
        if (!gameDir.exists()) {
            gameDir.mkdirs();
        }
        dataFile = new File(gameDir, "users.dat");

        // 获取偏好设置（用于记住密码）
        prefs = Preferences.userNodeForPackage(UserManager.class);

        loadUsers();

        // 如果没有用户，创建默认管理员账户
        if (users.isEmpty()) {
            register("admin", "admin");
        }
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    /**
     * 对密码进行MD5加密
     */
    private String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // 如果MD5不可用，返回原字符串（基本不会发生）
            return password;
        }
    }

    /**
     * 注册新用户
     * @return 注册结果：null表示成功，否则返回错误信息
     */
    public String register(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (password == null || password.trim().isEmpty()) {
            return "密码不能为空";
        }
        if (username.length() < 3) {
            return "用户名至少3个字符";
        }
        if (password.length() < 3) {
            return "密码至少3个字符";
        }
        if (users.containsKey(username)) {
            return "用户名已存在";
        }

        User newUser = new User(username, encryptPassword(password));
        users.put(username, newUser);
        saveUsers();
        return null;
    }

    /**
     * 用户登录
     * @return 登录结果：null表示成功，否则返回错误信息
     */
    public String login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return "请输入用户名";
        }
        if (password == null || password.trim().isEmpty()) {
            return "请输入密码";
        }

        User user = users.get(username);
        if (user == null) {
            return "用户名不存在";
        }
        if (!user.getPassword().equals(encryptPassword(password))) {
            return "密码错误";
        }

        currentUser = user;
        return null;
    }

    /**
     * 登出
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * 检查是否已登录
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * 更新用户游戏数据
     */
    public void updateGameData(int score) {
        if (currentUser != null) {
            currentUser.incrementTotalGames();
            currentUser.addTotalScore(score);
            currentUser.updateHighestScore(score);
            saveUsers();
        }
    }

    /**
     * 保存所有用户数据到文件
     */
    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
            oos.writeObject(new HashMap<>(users));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从文件加载用户数据
     */
    @SuppressWarnings("unchecked")
    private void loadUsers() {
        if (dataFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
                users = (HashMap<String, User>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                users = new HashMap<>();
            }
        }
    }

    /**
     * 保存记住密码的设置
     */
    public void saveRememberMe(String username, String password, boolean remember) {
        prefs.putBoolean(REMEMBER_USER, remember);
        if (remember && username != null) {
            prefs.put(SAVED_USERNAME, username);
            prefs.put(SAVED_PASSWORD, password);
        } else {
            prefs.remove(SAVED_USERNAME);
            prefs.remove(SAVED_PASSWORD);
        }
    }

    /**
     * 获取记住的用户名
     */
    public String getSavedUsername() {
        if (prefs.getBoolean(REMEMBER_USER, false)) {
            return prefs.get(SAVED_USERNAME, "");
        }
        return null;
    }

    /**
     * 获取记住的密码
     */
    public String getSavedPassword() {
        if (prefs.getBoolean(REMEMBER_USER, false)) {
            return prefs.get(SAVED_PASSWORD, "");
        }
        return null;
    }

    /**
     * 是否记住密码
     */
    public boolean isRememberMe() {
        return prefs.getBoolean(REMEMBER_USER, false);
    }

    /**
     * 获取所有用户列表（用于排行榜）
     */
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>(users.values());
        userList.sort((u1, u2) -> Integer.compare(u2.getHighestScore(), u1.getHighestScore()));
        return userList;
    }
}