package cn.edu.whut.sept.zuul;

import java.io.Serializable;


import java.io.Serializable;

/**
 * 用户数据模型
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private int highestScore;
    private int totalGames;
    private int totalScore;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.highestScore = 0;
        this.totalGames = 0;
        this.totalScore = 0;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public void updateHighestScore(int score) {
        if (score > highestScore) {
            highestScore = score;
        }
    }

    public int getTotalGames() {
        return totalGames;
    }

    public void incrementTotalGames() {
        totalGames++;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void addTotalScore(int score) {
        totalScore += score;
    }

    public double getAverageScore() {
        if (totalGames == 0) return 0;
        return (double) totalScore / totalGames;
    }

    @Override
    public String toString() {
        return username + "|" + password + "|" + highestScore + "|" + totalGames + "|" + totalScore;
    }

    public static User fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 5) {
            User user = new User(parts[0], parts[1]);
            user.highestScore = Integer.parseInt(parts[2]);
            user.totalGames = Integer.parseInt(parts[3]);
            user.totalScore = Integer.parseInt(parts[4]);
            return user;
        }
        return null;
    }
}