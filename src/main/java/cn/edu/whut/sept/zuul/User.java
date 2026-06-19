package cn.edu.whut.sept.zuul;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** 用户数据模型 - 支持数据库和本地存储 */
public class User implements Serializable {
  private static final long serialVersionUID = 1L;

  private String username;
  private String password; // 存储加密后的密码
  private int highestScore;
  private int totalGames;
  private int totalScore;

  // ========== 构造方法 ==========

  /** 构造方法（用于注册 - 明文密码自动加密） */
  public User(String username, String password) {
    this.username = username;
    this.password = encryptPassword(password);
    this.highestScore = 0;
    this.totalGames = 0;
    this.totalScore = 0;
  }

  /** 构造方法（用于从数据库加载 - 直接传入已加密密码） */
  public User(String username, String encryptedPassword, boolean alreadyEncrypted) {
    this.username = username;
    this.password = encryptedPassword; // 假定已加密
    this.highestScore = 0;
    this.totalGames = 0;
    this.totalScore = 0;
  }

  // ========== 密码加密工具 ==========

  private static String encryptPassword(String password) {
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
      return password; // fallback
    }
  }

  // ========== Getter / Setter ==========

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  /** 设置密码（不加密，直接存储，仅用于从数据库加载已加密密码） */
  public void setPassword(String password) {
    this.password = password;
  }

  public int getHighestScore() {
    return highestScore;
  }

  public void setHighestScore(int highestScore) {
    this.highestScore = highestScore;
  }

  public void updateHighestScore(int score) {
    if (score > highestScore) {
      highestScore = score;
    }
  }

  public int getTotalGames() {
    return totalGames;
  }

  public void setTotalGames(int totalGames) {
    this.totalGames = totalGames;
  }

  public void incrementTotalGames() {
    totalGames++;
  }

  public int getTotalScore() {
    return totalScore;
  }

  public void setTotalScore(int totalScore) {
    this.totalScore = totalScore;
  }

  public void addTotalScore(int score) {
    totalScore += score;
  }

  public double getAverageScore() {
    if (totalGames == 0) return 0;
    return (double) totalScore / totalGames;
  }

  // ========== 文件存储相关（保留，但不再使用） ==========

  @Override
  public String toString() {
    return username + "|" + password + "|" + highestScore + "|" + totalGames + "|" + totalScore;
  }

  public static User fromString(String line) {
    String[] parts = line.split("\\|");
    if (parts.length >= 5) {
      User user = new User(parts[0], parts[1], true); // 第三个参数表示密码已加密
      user.highestScore = Integer.parseInt(parts[2]);
      user.totalGames = Integer.parseInt(parts[3]);
      user.totalScore = Integer.parseInt(parts[4]);
      return user;
    }
    return null;
  }
}
