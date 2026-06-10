package cn.edu.whut.sept.zuul.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private Timer timer;
    private int playerX = 400, playerY = 500;
    private int playerWidth = 40, playerHeight = 40;
    private int speed = 5;
    private int originalSpeed = 5;  // 用于加速道具恢复

    private boolean leftPressed, rightPressed, upPressed, downPressed;

    // 物品系统相关变量
    private List<Item> items;      // 当前房间的物品
    private int score;             // 总分（所有房间累计）

    // ========== 多房间系统 ==========
    private List<Room> rooms;              // 所有房间列表
    private int currentRoomIndex;          // 当前房间索引（0=第1关）
    private Door door;                     // 传送门
    private int itemsCollectedInRoom;      // 当前房间收集的物品数
    private boolean levelCompleted;        // 当前关卡是否已完成（防止重复进门）
    private String message;                // 提示消息
    private int messageTimer;              // 消息显示计时器

    // ========== 敌人系统相关变量 ==========
    private boolean invincible = false;      // 无敌状态
    private int invincibleTimer = 0;         // 无敌计时器（帧数）

    // ========== 倒计时系统 ==========
    private int timeLeft = 60;               // 剩余时间（秒）
    private boolean gameOver = false;        // 游戏是否结束
    private javax.swing.Timer countdownTimer;

    // ========== 道具系统 ==========
    private List<PowerUp> powerUps;          // 道具列表
    private Random random;

    // 道具效果状态
    private boolean doubleScore = false;     // 双倍分数
    private int doubleScoreTimer = 0;        // 双倍分数计时器（帧数）
    private int speedBoostTimer = 0;         // 加速计时器（帧数）

    // 图片变量
    private BufferedImage playerImage;
    private BufferedImage backgroundImage;
    private BufferedImage itemImage;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        random = new Random();

        // 初始化多房间系统
        initRooms();

        // 初始化当前房间（第1关）
        currentRoomIndex = 0;
        itemsCollectedInRoom = 0;
        levelCompleted = false;
        message = "";
        messageTimer = 0;
        loadCurrentRoom();

        // 初始化道具
        initPowerUps();

        // 创建传送门（右侧）
        door = new Door(750, 250, 40, 100);

        // 加载图片资源
        loadImages();

        timer = new Timer(1000 / 60, this);
        timer.start();
        setFocusable(true);
        requestFocusInWindow();

        // 倒计时定时器
        countdownTimer = new javax.swing.Timer(1000, e -> {
            if (!gameOver && !levelCompleted && timeLeft > 0) {
                timeLeft--;
                repaint();
                if (timeLeft <= 0) {
                    gameOver = true;
                    countdownTimer.stop();
                    showMessage("⏰ 时间到！游戏结束！", 180);
                }
            }
        });
        countdownTimer.start();
    }

    // ========== 初始化所有房间 ==========
    private void initRooms() {
        rooms = new ArrayList<>();

        // 房间1：新手关卡，3个物品，1个敌人
        Room room1 = new Room("新手森林", new Color(25, 60, 30), 1);
        room1.addItem(new Item("金色徽章", 100, 200));
        room1.addItem(new Item("魔法水晶", 500, 300));
        room1.addItem(new Item("生命药水", 300, 450));
        room1.addEnemy(new Enemy(300, 250, 2));
        rooms.add(room1);

        // 房间2：进阶关卡，4个物品，2个敌人
        Room room2 = new Room("冰霜洞穴", new Color(30, 40, 80), 2);
        room2.addItem(new Item("冰晶碎片", 150, 180));
        room2.addItem(new Item("霜之精华", 400, 120));
        room2.addItem(new Item("寒冰宝石", 620, 350));
        room2.addItem(new Item("暴风核心", 250, 520));
        room2.addEnemy(new Enemy(200, 200, 2));
        room2.addEnemy(new Enemy(600, 400, 3));
        rooms.add(room2);

        // 房间3：困难关卡，5个物品，3个敌人
        Room room3 = new Room("烈焰深渊", new Color(80, 30, 20), 3);
        room3.addItem(new Item("火焰符文", 120, 250));
        room3.addItem(new Item("熔岩之心", 550, 180));
        room3.addItem(new Item("凤凰羽毛", 380, 380));
        room3.addItem(new Item("龙鳞碎片", 680, 480));
        room3.addItem(new Item("永恒之火", 200, 80));
        room3.addEnemy(new Enemy(100, 150, 3));
        room3.addEnemy(new Enemy(500, 200, 3));
        room3.addEnemy(new Enemy(350, 500, 4));
        rooms.add(room3);
    }

    // ========== 初始化道具 ==========
    private void initPowerUps() {
        powerUps = new ArrayList<>();
        // 在当前房间随机生成3个道具
        powerUps.add(new PowerUp(200, 300, PowerUp.Type.SPEED));
        powerUps.add(new PowerUp(550, 250, PowerUp.Type.INVINCIBLE));
        powerUps.add(new PowerUp(400, 480, PowerUp.Type.DOUBLE));
    }

    // ========== 重置当前房间的道具 ==========
    private void resetPowerUpsForRoom() {
        powerUps.clear();
        // 根据房间难度生成不同数量的道具
        int powerUpCount = currentRoomIndex + 2; // 第1关2个，第2关3个，第3关4个
        for (int i = 0; i < powerUpCount; i++) {
            int x = 50 + random.nextInt(700);
            int y = 50 + random.nextInt(450);
            PowerUp.Type type;
            int typeRand = random.nextInt(3);
            if (typeRand == 0) type = PowerUp.Type.SPEED;
            else if (typeRand == 1) type = PowerUp.Type.INVINCIBLE;
            else type = PowerUp.Type.DOUBLE;
            powerUps.add(new PowerUp(x, y, type));
        }
    }

    // ========== 加载当前房间 ==========
    // ========== 加载当前房间 ==========
    private void loadCurrentRoom() {
        Room currentRoom = rooms.get(currentRoomIndex);
        // 复制当前房间的物品列表
        items = new ArrayList<>();
        for (Item item : currentRoom.getItems()) {
            items.add(item);
        }
        // 重置玩家位置
        playerX = 400;
        playerY = 500;
        // 重置关卡完成标志
        levelCompleted = false;
        // 重置道具效果
        doubleScore = false;
        doubleScoreTimer = 0;
        speedBoostTimer = 0;
        speed = originalSpeed;
        // 确保道具列表已初始化
        if (powerUps == null) {
            powerUps = new ArrayList<>();
        }
        resetPowerUpsForRoom();
        // 显示欢迎消息
        showMessage("进入 " + currentRoom.getName() + "！收集所有星星！", 120);
    }

    // ========== 切换到下一房间 ==========
    private void switchToNextRoom() {
        if (levelCompleted) return;

        // 检查是否是最后一关
        if (currentRoomIndex + 1 >= rooms.size()) {
            gameOver = true;
            countdownTimer.stop();
            showMessage("🎉 恭喜你完成了所有关卡！总分数: " + score + " 🎉", 180);
            levelCompleted = true;
            return;
        }

        // 切换到下一关
        currentRoomIndex++;
        loadCurrentRoom();
        itemsCollectedInRoom = 0;
        showMessage("⭐ 进入第 " + (currentRoomIndex + 1) + " 关！难度提升！⭐", 120);
    }

    // ========== 显示临时消息 ==========
    private void showMessage(String msg, int duration) {
        message = msg;
        messageTimer = duration;
    }

    // ========== 检查是否碰到门 ==========
    private void checkDoorEntry() {
        if (levelCompleted || gameOver) return;

        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        if (door.getBounds().intersects(playerRect)) {
            if (items.isEmpty()) {
                switchToNextRoom();
            } else {
                showMessage("还需要收集 " + items.size() + " 个星星才能进入下一关！", 60);
            }
        }
    }

    // ========== 加载图片资源 ==========
    private void loadImages() {
        try {
            playerImage = ImageIO.read(getClass().getResource("/player.png"));
            if (playerImage == null) {
                System.out.println("警告：找不到图片 /player.png，将使用红色方块代替");
            }
        } catch (Exception e) {
            System.out.println("玩家图片加载失败：" + e.getMessage());
        }

        try {
            backgroundImage = ImageIO.read(getClass().getResource("/background.png"));
            if (backgroundImage == null) {
                System.out.println("警告：找不到图片 /background.png，将使用渐变背景代替");
            }
        } catch (Exception e) {
            System.out.println("背景图片加载失败：" + e.getMessage());
        }

        try {
            itemImage = ImageIO.read(getClass().getResource("/item.png"));
            if (itemImage == null) {
                System.out.println("警告：找不到图片 /item.png，将使用星星代替");
            }
        } catch (Exception e) {
            System.out.println("物品图片加载失败：" + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) {
            repaint();
            return;
        }

        // 移动玩家
        if (leftPressed) playerX -= speed;
        if (rightPressed) playerX += speed;
        if (upPressed) playerY -= speed;
        if (downPressed) playerY += speed;

        // 边界限制
        if (playerX < 0) playerX = 0;
        if (playerX > 760) playerX = 760;
        if (playerY < 0) playerY = 0;
        if (playerY > 560) playerY = 560;

        // 更新道具效果计时
        updatePowerUpTimers();

        // 检查是否拾取物品
        checkPickup();

        // 检查是否拾取道具
        checkPowerUpPickup();

        // 检查是否碰到门
        checkDoorEntry();

        // 更新所有敌人位置
        Room currentRoom = rooms.get(currentRoomIndex);
        for (Enemy enemy : currentRoom.getEnemies()) {
            enemy.move();
            enemy.bounceIfNeeded(800, 600);
        }

        // 检查玩家与敌人的碰撞
        checkEnemyCollision();

        // 更新无敌计时
        if (invincibleTimer > 0) {
            invincibleTimer--;
            if (invincibleTimer <= 0) {
                invincible = false;
            }
        }

        // 更新消息计时器
        if (messageTimer > 0) {
            messageTimer--;
        }

        repaint();
    }

    // ========== 更新道具效果计时 ==========
    private void updatePowerUpTimers() {
        if (speedBoostTimer > 0) {
            speedBoostTimer--;
            if (speedBoostTimer <= 0) {
                speed = originalSpeed;
            }
        }
        if (doubleScoreTimer > 0) {
            doubleScoreTimer--;
            if (doubleScoreTimer <= 0) {
                doubleScore = false;
            }
        }
    }

    // ========== 检查道具拾取 ==========
    private void checkPowerUpPickup() {
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        PowerUp toRemove = null;

        for (PowerUp p : powerUps) {
            if (playerRect.intersects(p.getBounds())) {
                toRemove = p;
                applyPowerUp(p.getType());
                break;
            }
        }

        if (toRemove != null) {
            powerUps.remove(toRemove);
            showMessage("✨ 获得道具！", 30);
        }
    }

    // ========== 应用道具效果 ==========
    private void applyPowerUp(PowerUp.Type type) {
        switch (type) {
            case SPEED:
                speed = originalSpeed + 3;
                speedBoostTimer = 300; // 5秒 (60帧 * 5)
                showMessage("⚡ 速度提升！持续5秒", 60);
                break;
            case INVINCIBLE:
                invincible = true;
                invincibleTimer = 180; // 3秒
                showMessage("✨ 无敌状态！持续3秒", 60);
                break;
            case DOUBLE:
                doubleScore = true;
                doubleScoreTimer = 300; // 5秒
                showMessage("2x 双倍分数！持续5秒", 60);
                break;
        }
    }

    // ========== 检查玩家与敌人碰撞 ==========
    private void checkEnemyCollision() {
        if (invincible) return;

        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        Room currentRoom = rooms.get(currentRoomIndex);

        for (Enemy enemy : currentRoom.getEnemies()) {
            if (playerRect.intersects(enemy.getBounds())) {
                int penalty = doubleScore ? 20 : 10;
                score -= penalty;
                if (score < 0) score = 0;

                playerX = 400;
                playerY = 500;

                invincible = true;
                invincibleTimer = 60;

                showMessage("💀 被敌人攻击！-" + penalty + "分！", 40);
                break;
            }
        }
    }

    // ========== 检查并处理物品拾取 ==========
    private void checkPickup() {
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        Item toRemove = null;

        for (Item item : items) {
            if (playerRect.intersects(item.getBounds())) {
                toRemove = item;
                int addScore = doubleScore ? 2 : 1;
                score += addScore;
                itemsCollectedInRoom++;
                break;
            }
        }

        if (toRemove != null) {
            items.remove(toRemove);
            String scoreMsg = doubleScore ? "+2 星星（双倍）！" : "+1 星星！";
            showMessage(scoreMsg + " 本关剩余: " + items.size(), 30);
        }
    }

    // ========== 绘制背景 ==========
    private void drawBackground(Graphics2D g) {
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, 800, 600, null);
        } else {
            Room currentRoom = rooms.get(currentRoomIndex);
            Color startColor = currentRoom.getBackgroundColor();
            Color endColor = new Color(
                    startColor.getRed() / 2,
                    startColor.getGreen() / 2,
                    startColor.getBlue() / 2
            );
            GradientPaint gradient = new GradientPaint(0, 0, startColor, 0, 600, endColor);
            g.setPaint(gradient);
            g.fillRect(0, 0, 800, 600);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 1. 背景
        drawBackground(g2d);

        // 2. 网格线
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i <= 800; i += 50) {
            g2d.drawLine(i, 0, i, 600);
            g2d.drawLine(0, i, 800, i);
        }

        // 3. 随机星星点缀
        g2d.setColor(new Color(255, 255, 200, 150));
        for (int i = 0; i < 100; i++) {
            int x = (i * 131) % 800;
            int y = (i * 253) % 600;
            g2d.fillOval(x, y, 2, 2);
        }

        // 4. 画门
        door.draw(g2d);

        // 5. 画道具
        for (PowerUp p : powerUps) {
            p.draw(g2d);
        }

        // 6. 画物品
        for (Item item : items) {
            drawItem(g2d, item.getX(), item.getY());
        }

        // 7. 画所有敌人
        Room currentRoom = rooms.get(currentRoomIndex);
        for (Enemy enemy : currentRoom.getEnemies()) {
            enemy.draw(g2d);
        }

        // 8. 画玩家（无敌闪烁效果）
        if (invincible && (invincibleTimer / 5) % 2 == 0) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        }
        if (playerImage != null) {
            g2d.drawImage(playerImage, playerX, playerY, playerWidth, playerHeight, null);
        } else {
            g2d.setColor(Color.RED);
            g2d.fillRect(playerX, playerY, playerWidth, playerHeight);
        }
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        // 9. 画UI
        drawUI(g2d);

        // 10. 画临时消息
        if (messageTimer > 0 && !message.isEmpty()) {
            drawMessage(g2d);
        }

        // 11. 画游戏结束画面
        if (gameOver) {
            drawGameOver(g2d);
        }
    }

    // ========== 绘制游戏结束画面 ==========
    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, 800, 600);
        g.setFont(new Font("微软雅黑", Font.BOLD, 48));
        if (timeLeft <= 0 && !levelCompleted) {
            g.setColor(Color.RED);
            g.drawString("⏰ 时间到！", 280, 280);
        } else if (levelCompleted && currentRoomIndex + 1 >= rooms.size()) {
            g.setColor(Color.YELLOW);
            g.drawString("🎉 通关胜利！", 280, 280);
        } else {
            g.setColor(Color.RED);
            g.drawString("💀 游戏结束", 300, 280);
        }
        g.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        g.setColor(Color.WHITE);
        g.drawString("最终得分: " + score, 340, 350);
        g.drawString("按 R 键重新开始", 330, 420);
    }

    // ========== 绘制临时消息 ==========
    private void drawMessage(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(200, 250, 400, 60, 20, 20);
        g.setFont(new Font("微软雅黑", Font.BOLD, 18));
        g.setColor(new Color(255, 220, 100));
        FontMetrics fm = g.getFontMetrics();
        int msgWidth = fm.stringWidth(message);
        g.drawString(message, 400 - msgWidth / 2, 290);
    }

    // ========== 绘制物品 ==========
    private void drawItem(Graphics2D g, int x, int y) {
        if (itemImage != null) {
            g.drawImage(itemImage, x, y, 32, 32, null);
        } else {
            g.setColor(new Color(255, 220, 100, 100));
            g.fillOval(x - 3, y - 3, 26, 26);
            g.setColor(new Color(255, 200, 0));
            g.fillOval(x, y, 20, 20);
            g.setColor(new Color(255, 255, 200));
            g.fillOval(x + 4, y + 4, 6, 6);
            g.setColor(Color.WHITE);
            for (int i = 0; i < 5; i++) {
                int angle = i * 72;
                int dx = (int) (Math.cos(Math.toRadians(angle)) * 12);
                int dy = (int) (Math.sin(Math.toRadians(angle)) * 12);
                g.fillOval(x + 10 + dx - 2, y + 10 + dy - 2, 3, 3);
            }
        }
    }

    // ========== 绘制UI面板 ==========
    private void drawUI(Graphics2D g) {
        Room currentRoom = rooms.get(currentRoomIndex);

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(10, 10, 780, 110, 25, 25);

        g.setColor(new Color(100, 180, 250, 180));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(10, 10, 780, 110, 25, 25);

        // 分数
        g.setFont(new Font("微软雅黑", Font.BOLD, 24));
        g.setColor(Color.WHITE);
        g.drawString("⭐ 总分: " + score, 30, 45);

        // 倒计时（颜色随剩余时间变化）
        g.setFont(new Font("微软雅黑", Font.BOLD, 24));
        if (timeLeft <= 10) {
            g.setColor(Color.RED);
        } else {
            g.setColor(Color.WHITE);
        }
        g.drawString("⏱️ " + timeLeft + "秒", 250, 45);

        // 当前关卡和剩余物品
        g.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        g.setColor(new Color(200, 200, 200));
        g.drawString("🏠 " + currentRoom.getName() + "   📦 剩余: " + items.size(), 30, 80);

        // 道具效果提示
        int y = 80;
        g.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        if (speedBoostTimer > 0) {
            g.setColor(Color.GREEN);
            g.drawString("⚡ 加速中", 400, y);
            y += 18;
        }
        if (invincibleTimer > 0) {
            g.setColor(Color.MAGENTA);
            g.drawString("✨ 无敌中", 400, y);
            y += 18;
        }
        if (doubleScoreTimer > 0) {
            g.setColor(Color.YELLOW);
            g.drawString("2x 双倍分数", 400, y);
        }

        // 提示
        g.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        g.setColor(new Color(200, 200, 200));
        g.drawString("方向键移动  收集完星星后进入右侧光门", 520, 55);
    }

    // 键盘按下
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // 按 R 键重新开始（优先执行）
        if (key == KeyEvent.VK_R) {
            restartGame();
            return;
        }

        // 移动控制
        if (key == KeyEvent.VK_LEFT) leftPressed = true;
        if (key == KeyEvent.VK_RIGHT) rightPressed = true;
        if (key == KeyEvent.VK_UP) upPressed = true;
        if (key == KeyEvent.VK_DOWN) downPressed = true;
    }

    // ========== 重新开始游戏 ==========
    private void restartGame() {
        // 重置游戏状态
        currentRoomIndex = 0;
        score = 0;
        timeLeft = 60;
        gameOver = false;
        levelCompleted = false;

        // 重置道具效果
        doubleScore = false;
        doubleScoreTimer = 0;
        speedBoostTimer = 0;
        speed = originalSpeed;
        invincible = false;
        invincibleTimer = 0;

        // 重置道具列表
        if (powerUps == null) {
            powerUps = new ArrayList<>();
        } else {
            powerUps.clear();
        }

        // 重新加载当前房间
        loadCurrentRoom();

        // 重新生成道具
        resetPowerUpsForRoom();

        // 重启倒计时
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        countdownTimer = new javax.swing.Timer(1000, e -> {
            if (!gameOver && !levelCompleted && timeLeft > 0) {
                timeLeft--;
                repaint();
                if (timeLeft <= 0) {
                    gameOver = true;
                    countdownTimer.stop();
                    showMessage("⏰ 时间到！游戏结束！", 180);
                }
            }
        });
        countdownTimer.start();

        // 显示欢迎消息
        showMessage("游戏重新开始！加油！", 60);
        repaint();
    }
    // 键盘松开
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) leftPressed = false;
        if (key == KeyEvent.VK_RIGHT) rightPressed = false;
        if (key == KeyEvent.VK_UP) upPressed = false;
        if (key == KeyEvent.VK_DOWN) downPressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}