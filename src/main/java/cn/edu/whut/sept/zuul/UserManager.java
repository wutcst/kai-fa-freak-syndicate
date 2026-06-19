package cn.edu.whut.sept.zuul;

import java.sql.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.prefs.Preferences;

/**
 * 用户管理器 - 使用 MySQL 数据库存储用户数据
 */
public class UserManager {
    private static UserManager instance;
    private User currentUser;
    private Preferences prefs;

    // ============================================================
    // ⚠️ 重要：请修改为你的 MySQL 连接信息
    // ============================================================
    private static final String DB_URL = "jdbc:mysql://localhost:3306/zuul_game?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";        // 你的 MySQL 用户名
    private static final String DB_PASS = "Sltz55555!";      // 你的 MySQL 密码（改成你自己的！）

    private static final String REMEMBER_USER = "remember_user";
    private static final String SAVED_USERNAME = "saved_username";
    private static final String SAVED_PASSWORD = "saved_password";

    private UserManager() {
        prefs = Preferences.userNodeForPackage(UserManager.class);
        createTableIfNotExists();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "username VARCHAR(50) PRIMARY KEY," +
                "password VARCHAR(32) NOT NULL," +
                "highest_score INT DEFAULT 0," +
                "total_games INT DEFAULT 0," +
                "total_score INT DEFAULT 0)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ 数据库连接成功，表已就绪");
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败，请检查：");
            System.err.println("   1. MySQL 服务是否启动");
            System.err.println("   2. 用户名密码是否正确");
            System.err.println("   3. 数据库 zuul_game 是否已创建");
            e.printStackTrace();
        }
    }

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
            return password;
        }
    }

    public String register(String username, String password) {
        if (username == null || username.trim().isEmpty()) return "用户名不能为空";
        if (password == null || password.trim().isEmpty()) return "密码不能为空";
        if (username.length() < 3) return "用户名至少3个字符";
        if (password.length() < 3) return "密码至少3个字符";

        // 检查用户是否存在
        String checkSql = "SELECT username FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return "用户名已存在";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "数据库错误";
        }

        // 插入新用户
        String insertSql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, username);
            ps.setString(2, encryptPassword(password));
            ps.executeUpdate();
            return null; // 成功
        } catch (SQLException e) {
            e.printStackTrace();
            return "注册失败，请稍后重试";
        }
    }

    public String login(String username, String password) {
        if (username == null || username.trim().isEmpty()) return "请输入用户名";
        if (password == null || password.trim().isEmpty()) return "请输入密码";

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, encryptPassword(password));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // 注意：这里只声明一次 user 变量
                User user = new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        true  // 密码已加密
                );
                user.setHighestScore(rs.getInt("highest_score"));
                user.setTotalGames(rs.getInt("total_games"));
                user.setTotalScore(rs.getInt("total_score"));
                this.currentUser = user;
                return null;
            } else {
                return "用户名或密码错误";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "数据库连接错误";
        }
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void updateGameData(int score) {
        if (currentUser == null) return;
        String sql = "UPDATE users SET highest_score = GREATEST(highest_score, ?), " +
                "total_games = total_games + 1, total_score = total_score + ? WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, score);
            ps.setInt(2, score);
            ps.setString(3, currentUser.getUsername());
            ps.executeUpdate();
            // 更新内存中的对象
            currentUser.updateHighestScore(score);
            currentUser.incrementTotalGames();
            currentUser.addTotalScore(score);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY highest_score DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        true
                );
                user.setHighestScore(rs.getInt("highest_score"));
                user.setTotalGames(rs.getInt("total_games"));
                user.setTotalScore(rs.getInt("total_score"));
                list.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ===== 记住密码相关 =====
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

    public String getSavedUsername() {
        if (prefs.getBoolean(REMEMBER_USER, false)) {
            return prefs.get(SAVED_USERNAME, "");
        }
        return null;
    }

    public String getSavedPassword() {
        if (prefs.getBoolean(REMEMBER_USER, false)) {
            return prefs.get(SAVED_PASSWORD, "");
        }
        return null;
    }

    public boolean isRememberMe() {
        return prefs.getBoolean(REMEMBER_USER, false);
    }
}