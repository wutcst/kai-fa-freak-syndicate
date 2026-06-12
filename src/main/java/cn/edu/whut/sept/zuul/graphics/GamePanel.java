package cn.edu.whut.sept.zuul.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import cn.edu.whut.sept.zuul.UserManager;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    // ===================== 全局常量 - 配色&尺寸 =====================
    private final int LEFT_PANEL_WIDTH = 180;
    private final int RIGHT_PANEL_WIDTH = 200;
    private final int BOTTOM_PANEL_HEIGHT = 30;

    // 配色体系 统一美化
    private final Color BG_MAIN = new Color(18, 22, 30);
    private final Color BG_PANEL = new Color(25, 30, 42);
    private final Color BORDER_COLOR = new Color(90, 160, 230);
    private final Color TEXT_MAIN = new Color(220, 220, 220);
    private final Color TEXT_HIGHLIGHT = new Color(255, 210, 120);

    // ===================== 游戏核心变量（全部保留原有） =====================
    private Timer timer;
    private int playerX = 400, playerY = 400;
    private int playerWidth = 30, playerHeight = 30;
    private int speed = 5;
    private int originalSpeed = 5;
    private boolean leftPressed, rightPressed, upPressed, downPressed;
    private boolean spacePressed = false;
    private List<Item> items;
    private List<Obstacle> obstacles;
    private int score;
    private List<Room> rooms;
    private int currentRoomIndex;
    private List<Door> doors;
    private List<KeyDoor> keyDoors;
    private boolean levelCompleted;
    private String message;
    private int messageTimer;
    private List<PowerUp> powerUps;
    private Random random;
    private boolean doubleScore = false;
    private int doubleScoreTimer = 0;
    private int speedBoostTimer = 0;
    private int health = 100;
    private int maxHealth = 100;
    private Player player;
    private Item nearbyItem = null;
    private boolean inventoryUIVisible = false;
    private Map<Room, List<Item>> roomStateMap = new HashMap<>();
    private boolean hasKey = false;
    private boolean ghostMode = false;
    private int ghostModeTimer = 0;
    private BufferedImage playerImage;
    private String playerName;
    private boolean isGuestMode;

    // ===================== 新增：独立布局容器（解决遮挡核心） =====================
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel bottomPanel;
    private JPanel gameCanvas;

    // 在现有变量声明后添加
    private List<Enemy> enemies;      // 敌人列表
    private boolean gameOverByEnemy;  // 是否被敌人击杀
    private int invincibleFrames = 0;      // 无敌帧计数（受伤后短暂无敌）
    private static final int INVINCIBLE_DURATION = 30;  // 无敌持续时间（约0.5秒，60帧）
    private int stunTimer = 0;           // 敌人眩晕计时
    private static final int STUN_DURATION = 180;  // 眩晕持续时间（3秒，60帧 * 3 = 180）
    private int minSpeed = 2;                // 最慢速度（负重满时）
    private int currentSpeed = 5;            // 当前速度
    private boolean ignoreWeight = false;  // 新增：是否无视负重
    private int ignoreWeightTimer = 0;     // 新增：无视负重计时器
    private static final int IGNORE_WEIGHT_DURATION = 300;  // 5秒（60帧 * 5）
    private int itemGlowCounter = 0;

    // ===================== 构造方法 - 重构布局 =====================
    public GamePanel(boolean isGuestMode) {
        this.isGuestMode = isGuestMode;
        UserManager um = UserManager.getInstance();
        playerName = um.isLoggedIn() ? um.getCurrentUser().getUsername() : "游客";

        // 主面板布局
        setLayout(new BorderLayout(5, 5));
        setBackground(BG_MAIN);
        setFocusable(true);
        addKeyListener(this);
        random = new Random();

        // 1. 左侧玩家信息面板
        leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawLeftPanelContent((Graphics2D) g);
            }
        };
        leftPanel.setPreferredSize(new Dimension(LEFT_PANEL_WIDTH, 0));
        leftPanel.setMaximumSize(new Dimension(LEFT_PANEL_WIDTH, Integer.MAX_VALUE));
        leftPanel.setBackground(BG_PANEL);
        add(leftPanel, BorderLayout.WEST);

        // 2. 右侧房间/物品面板
        rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawRightPanelContent((Graphics2D) g);
            }
        };
        rightPanel.setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, 0));
        rightPanel.setMaximumSize(new Dimension(RIGHT_PANEL_WIDTH, Integer.MAX_VALUE));
        rightPanel.setBackground(BG_PANEL);
        add(rightPanel, BorderLayout.EAST);

        // 3. 底部操作提示栏
        bottomPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBottomContent((Graphics2D) g);
            }
        };
        bottomPanel.setPreferredSize(new Dimension(0, BOTTOM_PANEL_HEIGHT));
        bottomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, BOTTOM_PANEL_HEIGHT));
        bottomPanel.setBackground(BG_PANEL);
        add(bottomPanel, BorderLayout.SOUTH);

        // 4. 中间游戏画布（纯游戏绘制区，与UI完全隔离）
        gameCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintGameContent((Graphics2D) g);
            }
        };
        gameCanvas.setBackground(BG_MAIN);
        gameCanvas.setFocusable(true);
        gameCanvas.addKeyListener(GamePanel.this);
        add(gameCanvas, BorderLayout.CENTER);


        // 初始化游戏
        initGame();
        loadPlayerImage();
        timer = new Timer(1000 / 60, this);
        timer.start();
        requestFocusInWindow();
    }

    // ===================== 游戏初始化（原有逻辑不变） =====================
    private void initGame() {
        initRooms();
        initObstacles();
        initDoors();
        initKeyDoors();
        currentRoomIndex = 0;
        levelCompleted = false;
        gameOverByEnemy = false;  // 新增
        message = "";
        messageTimer = 0;
        loadCurrentRoom();
        player = new Player(gameCanvas.getWidth()/2, gameCanvas.getHeight()/2);

        // 初始化敌人列表
        enemies = new ArrayList<>();
        spawnEnemyForCurrentRoom();  // 新增：为当前房间生成敌人

        health = maxHealth;
        score = 0;
        hasKey = false;
        ghostMode = false;
        ghostModeTimer = 0;

        regenerateObstacles();
        redistributeItems();
        gameCanvas.repaint();
        leftPanel.repaint();
        rightPanel.repaint();
    }

    private void initRooms() {
        rooms = new ArrayList<>();

        // ========== 房间1：森林 ==========
        Room room1 = new Room("森林", new Color(30, 60, 30), 1);
        room1.addItem(new Item("金色徽章", "闪亮的徽章", 5, 100, 100));
        room1.addItem(new Item("金色徽章", "闪亮的徽章", 5, 300, 150));
        room1.addItem(new Item("魔法水晶", "紫色水晶", 6, 700, 150));
        room1.addItem(new Item("魔法水晶", "紫色水晶", 6, 500, 300));
        room1.addItem(new Item("生命药水", "恢复20生命", 3, 300, 550));
        room1.addItem(new Item("生命药水", "恢复20生命", 3, 600, 450));
        room1.addItem(new Item("魔法饼干", "增加负重上限", 2, 650, 500));
        room1.addItem(new Item("幽灵药水", "5秒内穿过障碍物", 3, 500, 400));
        room1.addItem(new Item("加速药水", "5秒内无视负重", 2, 450, 200));  // 新增
        room1.addItem(new Item("钥匙", "打开锁着的门", 4, 200, 300));
        room1.addItem(new Item("眩晕药水", "使敌人眩晕3秒", 3, 400, 250));
        room1.addItem(new Item("眩晕药水", "使敌人眩晕3秒", 3, 150, 500));
        rooms.add(room1);

        // ========== 房间2：洞穴 ==========
        Room room2 = new Room("洞穴", new Color(60, 50, 40), 2);
        room2.addItem(new Item("冰晶碎片", "散发寒气", 4, 120, 180));
        room2.addItem(new Item("冰晶碎片", "散发寒气", 4, 400, 300));
        room2.addItem(new Item("霜之精华", "冰霜精华", 5, 680, 220));
        room2.addItem(new Item("寒冰宝石", "珍贵宝石", 7, 550, 500));
        room2.addItem(new Item("寒冰宝石", "珍贵宝石", 7, 200, 400));
        room2.addItem(new Item("生命药水", "恢复20生命", 3, 400, 80));
        room2.addItem(new Item("生命药水", "恢复20生命", 3, 600, 550));
        room2.addItem(new Item("幽灵药水", "5秒内穿过障碍物", 3, 300, 450));
        room2.addItem(new Item("加速药水", "5秒内无视负重", 2, 550, 300));  // 新增
        room2.addItem(new Item("魔法饼干", "增加负重上限", 2, 500, 200));
        room2.addItem(new Item("眩晕药水", "使敌人眩晕3秒", 3, 250, 350));
        room2.addItem(new Item("眩晕药水", "使敌人眩晕3秒", 3, 650, 150));
        rooms.add(room2);

        // ========== 房间3：深渊 ==========
        Room room3 = new Room("深渊", new Color(80, 30, 20), 3);
        room3.addItem(new Item("火焰符文", "火焰符文", 5, 100, 250));
        room3.addItem(new Item("火焰符文", "火焰符文", 5, 500, 200));
        room3.addItem(new Item("熔岩之心", "滚烫核心", 8, 700, 350));
        room3.addItem(new Item("凤凰羽毛", "传说羽毛", 3, 400, 550));
        room3.addItem(new Item("凤凰羽毛", "传说羽毛", 3, 250, 100));
        room3.addItem(new Item("魔法饼干", "增加负重上限", 2, 250, 100));
        room3.addItem(new Item("魔法饼干", "增加负重上限", 2, 600, 400));
        room3.addItem(new Item("生命药水", "恢复20生命", 3, 300, 300));
        room3.addItem(new Item("加速药水", "5秒内无视负重", 2, 480, 450));  // 新增
        room3.addItem(new Item("钥匙", "打开锁着的门", 4, 600, 500));
        room3.addItem(new Item("眩晕药水", "使敌人眩晕3秒", 3, 150, 400));
        room3.addItem(new Item("眩晕药水", "使敌人眩晕3秒", 3, 550, 250));
        rooms.add(room3);
    }

    private void initObstacles() {
        obstacles = new ArrayList<>();
    }

    private void initDoors() {
        doors = new ArrayList<>();
    }

    private void initKeyDoors() {
        keyDoors = new ArrayList<>();
    }

    private void loadCurrentRoom() {
        Room currentRoom = rooms.get(currentRoomIndex);
        if (roomStateMap.containsKey(currentRoom)) {
            items = new ArrayList<>();
            for (Item item : roomStateMap.get(currentRoom)) items.add(item);
        } else {
            items = new ArrayList<>();
            for (Item item : currentRoom.getItems()) items.add(item);
        }

        // 修改这里：确保玩家在中央
        // 使用 SwingUtilities.invokeLater 确保画布尺寸已就绪
        SwingUtilities.invokeLater(() -> {
            if (gameCanvas.getWidth() > 0 && gameCanvas.getHeight() > 0) {
                playerX = gameCanvas.getWidth() / 2 - playerWidth/2;
                playerY = gameCanvas.getHeight() / 2 - playerHeight/2;
            } else {
                // 默认值，等第一次绘制时会重新调整
                playerX = 400;
                playerY = 400;
            }
            if (player != null) player.setPosition(playerX, playerY);
            gameCanvas.repaint();
        });

        levelCompleted = false;
        regenerateObstacles();
        redistributeItems();
        gameCanvas.repaint();
        leftPanel.repaint();
        rightPanel.repaint();
    }

    /**
     * 为当前房间生成一个敌人
     */
    private void spawnEnemyForCurrentRoom() {
        if (enemies == null) {
            enemies = new ArrayList<>();
        }
        enemies.clear();

        // 确保画布尺寸有效
        if (gameCanvas.getWidth() <= 0 || gameCanvas.getHeight() <= 0) {
            // 延迟生成
            SwingUtilities.invokeLater(() -> spawnEnemyForCurrentRoom());
            return;
        }

        int roomX = 10;
        int roomY = 10;
        int roomW = gameCanvas.getWidth() - 20;
        int roomH = gameCanvas.getHeight() - 20;

        int enemyX, enemyY;
        int side = new Random().nextInt(4);

        switch(side) {
            case 0:
                enemyX = roomX + roomW/2 - 17;
                enemyY = roomY - 10;
                break;
            case 1:
                enemyX = roomX + roomW - 10;
                enemyY = roomY + roomH/2 - 17;
                break;
            case 2:
                enemyX = roomX + roomW/2 - 17;
                enemyY = roomY + roomH - 10;
                break;
            default:
                enemyX = roomX - 10;
                enemyY = roomY + roomH/2 - 17;
                break;
        }

        Enemy enemy = new Enemy(enemyX, enemyY);
        enemies.add(enemy);
        System.out.println("敌人生成在位置: (" + enemyX + ", " + enemyY + ")");  // 调试
    }

    /**
     * 根据当前负重计算玩家速度
     * 负重比例越高，速度越慢
     */
    // 在更新速度的方法中添加加速效果处理
    private void updateSpeedByWeight() {
        if (player == null) return;

        // 如果有加速药水效果，无视负重，直接最大速度
        if (ignoreWeight) {
            speed = originalSpeed;
            currentSpeed = originalSpeed;
            return;
        }

        // 如果有加速效果（原来的speed boost）
        if (speedBoostTimer > 0) {
            speed = originalSpeed + 3;
            currentSpeed = speed;
            return;
        }

        int currentWeight = player.getCurrentWeight();
        int maxWeight = player.getMaxWeight();

        if (maxWeight <= 0) {
            currentSpeed = originalSpeed;
            speed = currentSpeed;
            return;
        }

        double weightRatio = (double) currentWeight / maxWeight;
        int newSpeed = (int) (originalSpeed - (originalSpeed - minSpeed) * weightRatio);
        currentSpeed = Math.max(minSpeed, Math.min(originalSpeed, newSpeed));
        speed = currentSpeed;
    }

    /**
     * 更新所有敌人的位置（向玩家移动）
     */
    private void updateEnemies() {
        if (enemies == null || enemies.isEmpty() || levelCompleted) return;

        for (Enemy enemy : enemies) {
            // 更新眩晕状态
            enemy.updateStun();

            // 调试：打印眩晕状态变化
            // if (enemy.isStunned()) {
            //     System.out.println("敌人眩晕中...");
            // }

            // 向玩家方向移动（眩晕时不会移动）
            enemy.moveTowardsPlayer(playerX, playerY);

            // 边界限制
            int minX = 10 - 15;
            int maxX = gameCanvas.getWidth() - enemy.getWidth() - 10 + 15;
            int minY = 10 - 15;
            int maxY = gameCanvas.getHeight() - enemy.getHeight() - 10 + 15;

            int enemyX = enemy.getX();
            int enemyY = enemy.getY();
            enemyX = Math.max(minX, Math.min(maxX, enemyX));
            enemyY = Math.max(minY, Math.min(maxY, enemyY));
            enemy.setPosition(enemyX, enemyY);
        }
    }

    /**
     * 检查敌人与玩家的碰撞
     */
    private void checkEnemyCollision() {
        if (enemies == null || enemies.isEmpty() || levelCompleted) return;
        // 删除 ghostMode 检查 - 幽灵模式也会被敌人杀死

        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);

        for (Enemy enemy : enemies) {
            if (playerRect.intersects(enemy.getBounds())) {
                gameOverByEnemy = true;
                levelCompleted = true;
                showMessage("💀 你被红色追击者抓住了！游戏结束！💀", 180);

                if (!isGuestMode) {
                    UserManager.getInstance().updateGameData(score);
                }

                // 延迟重置游戏
                Timer delayTimer = new Timer(2000, e -> {
                    restartGame();
                });
                delayTimer.setRepeats(false);
                delayTimer.start();
                return;
            }
        }
    }

    private void saveCurrentRoomState() {
        Room currentRoom = rooms.get(currentRoomIndex);
        List<Item> saved = new ArrayList<>();
        for (Item item : items) saved.add(item);
        roomStateMap.put(currentRoom, saved);
    }

    private void loadPlayerImage() {
        try {
            playerImage = ImageIO.read(getClass().getResource("/player.png"));
        } catch (Exception e) {}
    }

    // ===================== 游戏逻辑（移动、碰撞、房间切换 全部保留） =====================
    private void checkDoors() {
        if (levelCompleted) return;
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        for (Door door : doors) {
            if (playerRect.intersects(door.getBounds())) {
                switchToNextRoom(door.getDirectionIndex());
                return;
            }
        }
    }

    private void checkKeyDoors() {
        if (levelCompleted) return;
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        for (KeyDoor kd : keyDoors) {
            if (kd.getRoomLevel() == currentRoomIndex + 1 && playerRect.intersects(kd.getBounds())) {
                if (hasKey) {
                    hasKey = false;
                    showMessage("钥匙消耗！门打开了！", 60);
                    switchToNextRoom(kd.getDirectionIndex());
                } else {
                    showMessage("门被锁住了！需要钥匙！", 60);
                }
                return;
            }
        }
    }

    private void switchToNextRoom(int direction) {
        if (levelCompleted) return;
        saveCurrentRoomState();
        if (currentRoomIndex + 1 >= rooms.size()) {
            gameOver();
            return;
        }
        currentRoomIndex++;
        loadCurrentRoom();

        // 新房间生成新敌人
        spawnEnemyForCurrentRoom();

        showMessage("进入 " + rooms.get(currentRoomIndex).getName() + "！", 60);
    }

    private void gameOver() {
        showMessage("🎉 通关胜利！总分数: " + score + " 🎉", 180);
        levelCompleted = true;
        if (!isGuestMode) {
            UserManager.getInstance().updateGameData(score);
            showMessage("分数已保存！最高分: " +
                    UserManager.getInstance().getCurrentUser().getHighestScore(), 120);
        }
    }

    private void showMessage(String msg, int duration) {
        message = msg;
        messageTimer = duration;
    }

    private void checkObstacleCollision() {
        if (ghostMode) return;
        if (invincibleFrames > 0) return;  // 无敌状态，不受伤害

        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);

        for (Obstacle obs : obstacles) {
            if (playerRect.intersects(obs.getBounds())) {

                // ========== 弹开效果（对所有障碍物） ==========
                int centerX = playerX + playerWidth/2;
                int centerY = playerY + playerHeight/2;
                int obsCenterX = obs.x + obs.w/2;
                int obsCenterY = obs.y + obs.h/2;

                int dx = centerX - obsCenterX;
                int dy = centerY - obsCenterY;

                if (Math.abs(dx) > Math.abs(dy)) {
                    if (dx > 0) {
                        playerX = obs.x + obs.w + 5;
                    } else {
                        playerX = obs.x - playerWidth - 5;
                    }
                } else {
                    if (dy > 0) {
                        playerY = obs.y + obs.h + 5;
                    } else {
                        playerY = obs.y - playerHeight - 5;
                    }
                }

                playerX = Math.max(10, Math.min(gameCanvas.getWidth() - playerWidth - 10, playerX));
                playerY = Math.max(10, Math.min(gameCanvas.getHeight() - playerHeight - 10, playerY));

                // ========== 伤害处理（仅尖刺） ==========
                if (obs.isSpike()) {
                    health -= 20;
                    invincibleFrames = INVINCIBLE_DURATION;

                    if (health <= 0) {
                        health = 0;
                        showMessage("💀 你被尖刺杀死了！游戏重新开始！💀", 60);
                        restartGame();
                        return;
                    } else {
                        showMessage("💔 触碰尖刺！-20生命！剩余: " + health + "/" + maxHealth, 40);
                    }
                }

                break;
            }
        }
    }

    private void checkNearbyItem() {
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        nearbyItem = null;
        for (Item item : items) {
            Rectangle itemRect = new Rectangle(item.getX(), item.getY(), 20, 20);
            Rectangle expanded = new Rectangle(playerRect.x - 50, playerRect.y - 50, playerRect.width + 100, playerRect.height + 100);
            if (expanded.intersects(itemRect)) {
                nearbyItem = item;
                break;
            }
        }
    }

    private void pickupNearbyItem() {
        if (nearbyItem == null) return;
        String name = nearbyItem.getName();

        if (name.equals("魔法饼干")) {
            player.increaseMaxWeight(5);
            items.remove(nearbyItem);
            showMessage("吃掉魔法饼干！负重上限+5！", 60);
        }
        else if (name.equals("生命药水")) {
            health = Math.min(maxHealth, health + 20);
            items.remove(nearbyItem);
            showMessage("生命+20！当前 " + health + "/" + maxHealth, 60);
        }
        else if (name.equals("幽灵药水")) {
            ghostMode = true;
            ghostModeTimer = 300;
            items.remove(nearbyItem);
            showMessage("幽灵模式！5秒内可穿过障碍物！", 60);
        }
        else if (name.equals("加速药水")) {  // 新增
            ignoreWeight = true;
            ignoreWeightTimer = IGNORE_WEIGHT_DURATION;
            items.remove(nearbyItem);
            showMessage("⚡ 加速药水！5秒内无视负重，速度达到最大！ ⚡", 60);
        }
        else if (name.equals("钥匙")) {
            hasKey = true;
            items.remove(nearbyItem);
            showMessage("获得了钥匙！可以打开锁着的门！", 60);
        }
        else if (name.equals("眩晕药水")) {
            if (enemies != null && !enemies.isEmpty()) {
                for (Enemy enemy : enemies) {
                    enemy.stun(STUN_DURATION);
                }
                items.remove(nearbyItem);
                showMessage("✨ 眩晕药水！敌人眩晕3秒！ ✨", 60);
            } else {
                showMessage("没有敌人可以眩晕...", 30);
            }
        }
        else {
            if (player.takeItem(nearbyItem)) {
                int add = doubleScore ? 2 : 1;
                score += add;
                items.remove(nearbyItem);
                showMessage("拾取 " + name + (doubleScore ? " +2分" : " +1分"), 60);
            } else {
                showMessage("太重了！负重：" + player.getCurrentWeight() + "/" + player.getMaxWeight(), 60);
            }
        }
        nearbyItem = null;
    }

    private void updatePowerUpTimers() {
        if (speedBoostTimer > 0) {
            speedBoostTimer--;
            if (speedBoostTimer <= 0) speed = originalSpeed;
        }
        if (doubleScoreTimer > 0) {
            doubleScoreTimer--;
            if (doubleScoreTimer <= 0) doubleScore = false;
        }
    }

    // ===================== 计时器主循环 60帧 =====================
    @Override
    public void actionPerformed(ActionEvent e) {
        if (levelCompleted) {
            gameCanvas.repaint();
            return;
        }

        // 更新各种计时器
        if (invincibleFrames > 0) {
            invincibleFrames--;
        }

        itemGlowCounter = (itemGlowCounter + 1) % 60;

        if (ignoreWeightTimer > 0) {
            ignoreWeightTimer--;
            if (ignoreWeightTimer <= 0) {
                ignoreWeight = false;
                showMessage("加速药水效果结束，速度恢复正常", 30);
            }
        }

        if (speedBoostTimer > 0) {
            speedBoostTimer--;
            if (speedBoostTimer <= 0) {
                speed = originalSpeed;
            }
        }

        if (ghostModeTimer > 0) {
            ghostModeTimer--;
            if (ghostModeTimer <= 0) {
                ghostMode = false;
                ejectFromObstacles();
                showMessage("幽灵模式结束", 30);
            }
        }

        // 根据负重更新速度
        updateSpeedByWeight();

        // 玩家移动
        if (leftPressed) playerX -= currentSpeed;
        if (rightPressed) playerX += currentSpeed;
        if (upPressed) playerY -= currentSpeed;
        if (downPressed) playerY += currentSpeed;

        // 边界限制（基于游戏画布，不再受侧边栏影响）
        int minX = 10 - 15;
        int maxX = gameCanvas.getWidth() - playerWidth - 10 + 15;
        int minY = 10 - 15;
        int maxY = gameCanvas.getHeight() - playerHeight - 10 + 15;
        playerX = Math.max(minX, Math.min(maxX, playerX));
        playerY = Math.max(minY, Math.min(maxY, playerY));
        if (player != null) player.setPosition(playerX, playerY);

        updatePowerUpTimers();
        if (ghostModeTimer > 0) {
            ghostModeTimer--;
            if (ghostModeTimer <= 0) {
                ghostMode = false;
                // 幽灵模式结束时，检查是否卡在障碍物中
                ejectFromObstacles();
                showMessage("幽灵模式结束", 30);
            }
        }

        checkObstacleCollision();
        // 在 checkObstacleCollision(); 之后添加
        updateEnemies();        // 更新敌人位置
        checkEnemyCollision();  // 检查敌人碰撞
        checkDoors();
        checkKeyDoors();
        checkNearbyItem();

        if (spacePressed) {
            pickupNearbyItem();
            spacePressed = false;
        }
        if (messageTimer > 0) messageTimer--;

        // 局部刷新，优化性能、防闪烁
        gameCanvas.repaint();
        leftPanel.repaint();
        rightPanel.repaint();
    }

    private void ejectFromObstacles() {
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        boolean stuck = true;
        int maxAttempts = 20;
        int attempt = 0;

        while (stuck && attempt < maxAttempts) {
            stuck = false;
            for (Obstacle obs : obstacles) {
                if (playerRect.intersects(obs.getBounds())) {
                    stuck = true;
                    // 尝试各种方向传送
                    int[][] offsets = {
                            {50, 0}, {-50, 0}, {0, 50}, {0, -50},     // 上下左右
                            {30, 30}, {-30, -30}, {30, -30}, {-30, 30} // 对角线
                    };

                    if (attempt < offsets.length) {
                        playerX += offsets[attempt][0];
                        playerY += offsets[attempt][1];
                    } else {
                        // 最后手段：传送到房间中央
                        playerX = gameCanvas.getWidth() / 2 - playerWidth/2;
                        playerY = gameCanvas.getHeight() / 2 - playerHeight/2;
                    }

                    // 边界限制
                    playerX = Math.max(10, Math.min(gameCanvas.getWidth() - playerWidth - 10, playerX));
                    playerY = Math.max(10, Math.min(gameCanvas.getHeight() - playerHeight - 10, playerY));

                    playerRect.setBounds(playerX, playerY, playerWidth, playerHeight);
                    break;
                }
            }
            attempt++;
        }

        // 如果还是卡住，强制传送到安全区域
        if (stuck) {
            playerX = gameCanvas.getWidth() / 2 - playerWidth/2;
            playerY = gameCanvas.getHeight() / 2 - playerHeight/2;
            showMessage("强制传送至安全区域", 60);
        }
    }

    // ===================== 分区绘制方法（核心：解决遮挡） =====================
    private void paintGameContent(Graphics2D g2d) {
        // 全局抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawGridBackground(g2d);
        drawRoomArea(g2d);
        updateDoorPositions();

        for (Obstacle obs : obstacles) obs.draw(g2d);
        for (Door door : doors) door.draw(g2d);
        for (KeyDoor kd : keyDoors) {
            if (kd.getRoomLevel() == currentRoomIndex + 1) kd.draw(g2d);
        }

        for (Item item : items) {
            drawItem(g2d, item.getX(), item.getY(), item);
        }

        if (enemies != null) {
            for (Enemy enemy : enemies) {
                enemy.draw(g2d);
            }
        }

        // 绘制玩家
        if (playerImage != null) {
            g2d.drawImage(playerImage, playerX, playerY, playerWidth, playerHeight, null);
        } else {
            g2d.setColor(new Color(70, 130, 200));
            g2d.fillRoundRect(playerX, playerY, playerWidth, playerHeight, 8, 8);
        }

        // 附近物品提示
        if (nearbyItem != null) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRoundRect(playerX - 10, playerY - 25, 70, 22, 8, 8);
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 11));
            g2d.drawString("空格拾取", playerX - 5, playerY - 10);
        }

        // 弹窗层级最高
        if (messageTimer > 0 && !message.isEmpty()) drawMessage(g2d);
        if (levelCompleted && currentRoomIndex + 1 >= rooms.size()) drawGameOver(g2d);
        if (inventoryUIVisible) drawInventoryUI(g2d);
    }

    private void setPlayerToCenter() {
        if (gameCanvas.getWidth() > 0 && gameCanvas.getHeight() > 0) {
            playerX = gameCanvas.getWidth() / 2 - playerWidth/2;
            playerY = gameCanvas.getHeight() / 2 - playerHeight/2;
        } else {
            playerX = 400;
            playerY = 400;
        }
        if (player != null) player.setPosition(playerX, playerY);
    }

    private void drawLeftPanelContent(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int x = 10;
        int y = 10;
        int w = leftPanel.getWidth() - 20;
        int h = leftPanel.getHeight() - 20;

        // 背景
        g.setColor(new Color(0, 0, 0, 230));
        g.fillRoundRect(x, y, w, h, 12, 12);
        g.setColor(BORDER_COLOR);
        g.drawRoundRect(x, y, w, h, 12, 12);

        int py = y + 20;

        // 标题
        g.setFont(new Font("微软雅黑", Font.BOLD, 14));
        g.setColor(TEXT_MAIN);
        g.drawString("冒险者", x + 10, py);
        py += 20;

        // 血量条
        g.setColor(new Color(80, 0, 0));
        g.fillRoundRect(x + 10, py, w - 20, 14, 7, 7);
        int hpPercent = health * (w - 20) / maxHealth;
        GradientPaint hpGrad = new GradientPaint(x+10,0,new Color(255,80,80),x+w-10,0,new Color(220,30,30));
        g.setPaint(hpGrad);
        g.fillRoundRect(x + 10, py, hpPercent, 14, 7, 7);
        g.setPaint(null);
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.BOLD, 10));
        g.drawString(health + "/" + maxHealth, x + w/2 - 20, py + 11);
        py += 20;

        // 分数
        g.setFont(new Font("微软雅黑", Font.BOLD, 14));
        g.setColor(TEXT_HIGHLIGHT);
        g.drawString("💰 " + score, x + 10, py);
        py += 20;

        // 负重条
        g.setColor(new Color(60, 40, 40));
        g.fillRoundRect(x + 10, py, w - 20, 12, 6, 6);
        int weightPercent = player.getCurrentWeight() * (w - 20) / player.getMaxWeight();

        // 根据负重比例改变颜色
        double weightRatio = (double) player.getCurrentWeight() / player.getMaxWeight();
        if (weightRatio > 0.8) {
            g.setColor(new Color(255, 80, 80));  // 红色 - 过重
        } else if (weightRatio > 0.5) {
            g.setColor(new Color(255, 180, 50)); // 橙色 - 较重
        } else {
            g.setColor(new Color(100, 200, 100)); // 绿色 - 正常
        }
        g.fillRoundRect(x + 10, py, weightPercent, 12, 6, 6);

        g.setColor(TEXT_MAIN);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 9));
        g.drawString("负重:" + player.getCurrentWeight() + "/" + player.getMaxWeight() + "kg", x + 10, py + 18);
        py += 30;

        // ========== 新增：速度显示 ==========
        g.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        if (currentSpeed < originalSpeed) {
            g.setColor(new Color(255, 150, 100));
            g.drawString("🐢 移动速度: " + currentSpeed + " (负重减速)", x + 10, py);
        } else if (speedBoostTimer > 0) {
            g.setColor(new Color(100, 255, 100));
            g.drawString("⚡ 移动速度: " + currentSpeed + " (加速中)", x + 10, py);
        } else {
            g.setColor(new Color(200, 200, 200));
            g.drawString("🏃 移动速度: " + currentSpeed, x + 10, py);
        }
        py += 18;

        // 负重警告
        if (weightRatio > 0.8) {
            g.setColor(new Color(255, 80, 80));
            g.setFont(new Font("微软雅黑", Font.BOLD, 11));
            g.drawString("⚠️ 负重过高！速度下降！", x + 10, py);
            py += 18;
        }

        // 状态提示
        g.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        if (speedBoostTimer > 0) {
            g.setColor(Color.GREEN);
            g.drawString("⚡加速中", x + 10, py);
            py += 18;
        }
        if (ghostMode) {
            g.setColor(Color.MAGENTA);
            g.drawString("👻幽灵模式", x + 10, py);
            py += 18;
        }
        if (ignoreWeight) {
            g.setColor(new Color(100, 255, 150));
            g.drawString("⚡ 加速药水效果中 (无视负重)", x + 10, py);
            py += 18;
        }
        if (ghostMode) {
            g.setColor(Color.MAGENTA);
            g.drawString("👻幽灵模式", x + 10, py);
            py += 18;
        }
        if (hasKey) {
            g.setColor(TEXT_HIGHLIGHT);
            g.drawString("🔑持有钥匙", x + 10, py);
            py += 18;
        }
        if (invincibleFrames > 0) {
            g.setColor(new Color(255, 255, 100));
            g.drawString("✨ 无敌状态", x + 10, py);
            py += 18;
        }
    }

    private void drawRightPanelContent(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 新增：专门承载emoji的字体（Windows自带，完美兼容所有表情）
        Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 14);
        Font textFont14 = new Font("微软雅黑", Font.BOLD, 14);
        Font textFont12 = new Font("微软雅黑", Font.BOLD, 12);
        Font textFont11 = new Font("微软雅黑", Font.PLAIN, 11);

        int x = 10;
        int y = 10;
        int w = getWidth() - 20;
        int h = getHeight() - 20;

        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(x, y, w, h, 12, 12);
        g.setColor(BORDER_COLOR);
        g.drawRoundRect(x, y, w, h, 12, 12);

        Room currentRoom = rooms.get(currentRoomIndex);
        // 1. 先画emoji图标
        g.setFont(emojiFont);
        g.setColor(Color.CYAN);
        g.drawString("🏠", x + 10, y + 25);
        // 2. 偏移X坐标，再画普通文字
        g.setFont(textFont14);
        g.drawString(" " + currentRoom.getName(), x + 30, y + 25);

        g.setFont(textFont11);
        g.setColor(new Color(200, 200, 150));
        g.drawString("玩家: " + playerName + (isGuestMode ? " (游客)" : ""), x + 10, y + 45);

        // 物品清单
        g.setFont(emojiFont);
        g.setColor(TEXT_HIGHLIGHT);
        g.drawString("📦", x + 10, y + 70);
        g.setFont(textFont12);
        g.drawString(" 物品清单 (" + items.size() + ")", x + 30, y + 70);

        int py = y + 90;
        g.setFont(textFont11);
        for (Item item : items) {
            if (py > y + h - 60) break;
            g.setColor(new Color(255, 220, 150));
            // 物品小宝石emoji
            g.setFont(emojiFont);
            g.drawString("🔸", x + 10, py);
            g.setFont(textFont11);
            g.drawString(" " + item.getName() + " (" + item.getWeight() + "kg)", x + 30, py);
            py += 18;
        }
        py += 10;

        // 房门出口
        g.setColor(Color.CYAN);
        g.setFont(emojiFont);
        g.drawString("🚪", x + 10, py);
        g.setFont(textFont12);
        g.drawString(" 房门出口", x + 30, py);
        py += 18;
        g.setColor(TEXT_MAIN);

        // 门方向绘制不变
        for (Door door : doors) {
            String dir = door.getDirection();
            String arrow = "";
            switch(dir) {
                case "north": arrow = "↑ 北"; break;
                case "south": arrow = "↓ 南"; break;
                case "east": arrow = "→ 东"; break;
                case "west": arrow = "← 西"; break;
            }
            g.drawString(arrow, x + 10, py);
            py += 16;
        }
        for (KeyDoor kd : keyDoors) {
            if (kd.getRoomLevel() == currentRoomIndex + 1) {
                String dir = kd.getDirection();
                String arrow = "";
                switch(dir) {
                    case "north": arrow = "↑ 北("; break;
                    case "south": arrow = "↓ 南("; break;
                    case "east": arrow = "→ 东("; break;
                    case "west": arrow = "← 西("; break;
                }
                g.setColor(Color.RED);
                g.drawString(arrow, x + 10, py);
                // 锁emoji单独画
                g.setFont(emojiFont);
                g.drawString("🔒", x + g.getFontMetrics(textFont11).stringWidth(arrow)+10, py);
                g.setFont(textFont11);
                g.drawString(")", x + g.getFontMetrics(textFont11).stringWidth(arrow)+25, py);
                py += 16;
            }
        }
    }

    private void drawBottomContent(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g.setColor(new Color(200, 200, 220));
        g.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        String tips = "WASD/方向键 移动   空格 拾取   I 背包   L 查看房间   B 返回上一房间   R 重新开始";
        int tipW = g.getFontMetrics().stringWidth(tips);
        g.drawString(tips, (getWidth() - tipW) / 2, 20);
    }

    // ===================== 原有绘制工具方法（适配新画布） =====================
    private void drawGridBackground(Graphics2D g) {
        g.setColor(new Color(30, 35, 45));
        g.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        int cell = 40;
        g.setColor(new Color(50, 55, 65));
        for (int x = 0; x < gameCanvas.getWidth(); x += cell) g.drawLine(x, 0, x, gameCanvas.getHeight());
        for (int y = 0; y < gameCanvas.getHeight(); y += cell) g.drawLine(0, y, gameCanvas.getWidth(), y);
    }

    private void drawRoomArea(Graphics2D g) {
        int roomX = 10;
        int roomY = 10;
        int roomW = gameCanvas.getWidth() - 20;
        int roomH = gameCanvas.getHeight() - 20;
        Room currentRoom = rooms.get(currentRoomIndex);
        g.setColor(currentRoom.getBackgroundColor());
        g.fillRoundRect(roomX, roomY, roomW, roomH, 20, 20);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(4));
        g.drawRoundRect(roomX, roomY, roomW, roomH, 20, 20);
    }

    private void updateDoorPositions() {
        int roomX = 10;
        int roomY = 10;
        int roomW = gameCanvas.getWidth() - 20;
        int roomH = gameCanvas.getHeight() - 20;
        int doorWidth = 40;
        int doorHeight = 60;
        if (doors.isEmpty()) {
            doors.add(new Door(0, 0, doorWidth, doorHeight, "west"));
            doors.add(new Door(0, 0, doorWidth, doorHeight, "east"));
            doors.add(new Door(0, 0, doorHeight, doorWidth, "north"));
            doors.add(new Door(0, 0, doorHeight, doorWidth, "south"));
        }
        for (Door door : doors) {
            switch (door.getDirection()) {
                case "west":
                    door.setPosition(roomX - 15, roomY + roomH/2 - doorHeight/2);
                    break;
                case "east":
                    door.setPosition(roomX + roomW - 25, roomY + roomH/2 - doorHeight/2);
                    break;
                case "north":
                    door.setPosition(roomX + roomW/2 - doorHeight/2, roomY - 15);
                    break;
                case "south":
                    door.setPosition(roomX + roomW/2 - doorHeight/2, roomY + roomH - 25);
                    break;
            }
        }
        if (keyDoors.isEmpty()) {
            keyDoors.add(new KeyDoor(0, 0, doorWidth, doorHeight, "east", 1));
            keyDoors.add(new KeyDoor(0, 0, doorWidth, doorHeight, "south", 2));
            keyDoors.add(new KeyDoor(0, 0, doorHeight, doorWidth, "north", 3));
        }
        for (KeyDoor kd : keyDoors) {
            switch (kd.getDirection()) {
                case "west":
                    if (kd.getRoomLevel() == currentRoomIndex + 1) {
                        kd.setPosition(roomX - 15, roomY + roomH/3);
                    }
                    break;
                case "east":
                    if (kd.getRoomLevel() == currentRoomIndex + 1) {
                        kd.setPosition(roomX + roomW - 25, roomY + roomH/3);
                    }
                    break;
                case "north":
                    if (kd.getRoomLevel() == currentRoomIndex + 1) {
                        kd.setPosition(roomX + roomW/3, roomY - 15);
                    }
                    break;
                case "south":
                    if (kd.getRoomLevel() == currentRoomIndex + 1) {
                        kd.setPosition(roomX + roomW/3, roomY + roomH - 25);
                    }
                    break;
            }
        }
    }

    private void drawItem(Graphics2D g, int x, int y, Item item) {
        String type = getItemType(item);
        String name = item.getName();

        // 在 switch 之前添加，让所有物品都有呼吸效果
        float alpha = 0.5f + (float)Math.sin(itemGlowCounter * Math.PI / 30) * 0.3f;

        // 根据不同类型绘制不同的外观
        switch(type) {
            case "buff":
                // 增益物品：绿色+ 光芒效果
                // 外发光
                g.setColor(new Color(100, 255, 100, 80));
                g.fillOval(x - 3, y - 3, 24, 24);
                // 主体
                g.setColor(new Color(50, 200, 50));
                g.fillOval(x, y, 18, 18);
                g.setColor(new Color(100, 255, 100));
                g.fillOval(x + 2, y + 2, 14, 14);
                // 加号标志
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2));
                g.drawLine(x + 9, y + 4, x + 9, y + 14);
                g.drawLine(x + 4, y + 9, x + 14, y + 9);
                break;

            case "stun":
                // 眩晕药水：紫色 + 星星效果
                g.setColor(new Color(200, 100, 255, 80));
                g.fillOval(x - 3, y - 3, 24, 24);
                g.setColor(new Color(160, 50, 200));
                g.fillOval(x, y, 18, 18);
                g.setColor(new Color(200, 100, 255));
                g.fillOval(x + 2, y + 2, 14, 14);
                // 星星标志
                g.setColor(Color.YELLOW);
                g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
                g.drawString("⭐", x + 4, y + 14);
                break;

            case "key":
                // 钥匙：金色 + 钥匙形状
                g.setColor(new Color(255, 200, 50, 80));
                g.fillOval(x - 3, y - 3, 24, 24);
                g.setColor(new Color(255, 180, 0));
                g.fillOval(x, y, 18, 18);
                g.setColor(new Color(255, 220, 100));
                g.fillOval(x + 2, y + 2, 14, 14);
                // 钥匙标志
                g.setColor(Color.WHITE);
                g.fillRect(x + 8, y + 5, 3, 10);
                g.fillOval(x + 6, y + 4, 7, 5);
                break;

            default:
                // 得分物品：蓝色/金色 + 宝石形状
                g.setColor(new Color(50, 150, 255, 80));
                g.fillOval(x - 3, y - 3, 24, 24);

                // 宝石渐变效果
                GradientPaint gemGrad = new GradientPaint(
                        x, y, new Color(50, 150, 255),
                        x + 18, y + 18, new Color(30, 100, 200)
                );
                g.setPaint(gemGrad);
                g.fillOval(x, y, 18, 18);

                // 高光
                g.setColor(new Color(150, 200, 255, 180));
                g.fillOval(x + 4, y + 3, 5, 5);

                // 分数标志
                g.setColor(Color.WHITE);
                g.setFont(new Font("微软雅黑", Font.BOLD, 10));
                g.drawString("+", x + 7, y + 13);
                break;
        }

        // 物品名称标签（带背景）
        g.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(name);

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(x + 18 - textWidth/2, y - 12, textWidth + 4, 14, 5, 5);
        g.setColor(Color.WHITE);
        g.drawString(name, x + 20 - textWidth/2, y - 2);
    }

    private void drawMessage(Graphics2D g) {
        int w = 500, h = 50;
        int x = (gameCanvas.getWidth() - w) / 2;
        int y = (gameCanvas.getHeight() - h) / 2;
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(x, y, w, h, 15, 15);
        g.setFont(new Font("微软雅黑", Font.BOLD, 16));
        g.setColor(new Color(255, 220, 100));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(message, x + (w - fm.stringWidth(message)) / 2, y + 32);
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 220));
        g.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        g.setFont(new Font("微软雅黑", Font.BOLD, 48));
        g.setColor(Color.YELLOW);
        String msg = "🎉 通关胜利！";
        g.drawString(msg, (gameCanvas.getWidth() - g.getFontMetrics().stringWidth(msg)) / 2, gameCanvas.getHeight() / 2 - 50);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 24));
        g.setColor(Color.WHITE);
        String scoreMsg = "最终得分: " + score;
        g.drawString(scoreMsg, (gameCanvas.getWidth() - g.getFontMetrics().stringWidth(scoreMsg)) / 2, gameCanvas.getHeight() / 2 + 20);
        String restart = "按 R 键重新开始";
        g.drawString(restart, (gameCanvas.getWidth() - g.getFontMetrics().stringWidth(restart)) / 2, gameCanvas.getHeight() / 2 + 80);
    }

    private void drawInventoryUI(Graphics2D g) {
        if (!inventoryUIVisible) return;
        g.setColor(new Color(0, 0, 0, 220));
        g.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        int pw = 500, ph = 400;
        int px = (gameCanvas.getWidth() - pw) / 2;
        int py = (gameCanvas.getHeight() - ph) / 2;
        g.setColor(new Color(40, 35, 55));
        g.fillRoundRect(px, py, pw, ph, 20, 20);
        g.setColor(new Color(150, 100, 200));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(px, py, pw, ph, 20, 20);
        g.setFont(new Font("微软雅黑", Font.BOLD, 24));
        g.setColor(Color.YELLOW);
        g.drawString("📦 背包", px + pw/2 - 60, py + 50);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        g.setColor(Color.WHITE);
        g.drawString("负重: " + player.getCurrentWeight() + "/" + player.getMaxWeight() + " kg", px + 150, py + 90);
        g.setColor(new Color(60, 40, 40));
        g.fillRoundRect(px + 110, py + 105, 280, 12, 6, 6);
        int weightPercent = player.getCurrentWeight() * 280 / player.getMaxWeight();
        g.setColor(new Color(255, 180, 50));
        g.fillRoundRect(px + 110, py + 105, weightPercent, 12, 6, 6);
        List<Item> inventory = player.getInventory();
        int y = py + 150;
        if (inventory.isEmpty()) {
            g.setColor(Color.GRAY);
            g.drawString("空空如也...", px + pw/2 - 40, y);
        } else {
            for (int i = 0; i < inventory.size(); i++) {
                Item item = inventory.get(i);
                if (y > py + ph - 50) break;
                g.setColor(new Color(255, 220, 150));
                g.drawString("● " + item.getName(), px + 50, y);
                g.setColor(new Color(180, 180, 200));
                g.drawString(item.getDescription(), px + 160, y);
                g.setColor(Color.YELLOW);
                g.drawString(item.getWeight() + "kg", px + 440, y);
                y += 35;
            }
        }
        g.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        g.setColor(new Color(150, 150, 200));
        g.drawString("按 I 关闭", px + pw/2 - 30, py + ph - 30);
    }

    // ===================== 按键监听 & 功能方法 =====================
    private void look() {
        if (items.isEmpty()) {
            showMessage("当前房间没有物品。", 120);
            return;
        }
        StringBuilder sb = new StringBuilder("房间里的物品：\n");
        int totalW = 0;
        for (Item item : items) {
            sb.append("  - ").append(item.getName()).append(" (").append(item.getWeight())
                    .append("kg): ").append(item.getDescription()).append("\n");
            totalW += item.getWeight();
        }
        sb.append("总重量：").append(totalW).append("kg");
        showMessage(sb.toString(), 180);
    }

    private void back() {
        if (currentRoomIndex > 0) {
            saveCurrentRoomState();
            currentRoomIndex--;
            loadCurrentRoom();

            // 返回上一个房间时也重新生成敌人
            spawnEnemyForCurrentRoom();

            showMessage("返回 " + rooms.get(currentRoomIndex).getName(), 60);
            setPlayerToCenter();
        } else {
            showMessage("已经在第一个房间，无法返回！", 60);
        }
    }

    private void showItems() {
        inventoryUIVisible = !inventoryUIVisible;
    }

    private void restartGame() {
        if (!isGuestMode && score > 0 && levelCompleted && !gameOverByEnemy) {
            UserManager.getInstance().updateGameData(score);
        }
        currentRoomIndex = 0;
        score = 0;
        health = maxHealth;
        levelCompleted = false;
        gameOverByEnemy = false;
        invincibleFrames = 0;
        ignoreWeight = false;      // 新增
        ignoreWeightTimer = 0;     // 新增
        roomStateMap.clear();
        hasKey = false;
        ghostMode = false;
        ghostModeTimer = 0;
        speedBoostTimer = 0;
        doubleScore = false;
        doubleScoreTimer = 0;
        player = new Player(400, 400);

        enemies = new ArrayList<>();
        spawnEnemyForCurrentRoom();

        loadCurrentRoom();
        showMessage("游戏重新开始！", 60);
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        e.consume();
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_R) { restartGame(); return; }
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) leftPressed = true;
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) rightPressed = true;
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) upPressed = true;
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) downPressed = true;
        if (key == KeyEvent.VK_SPACE) spacePressed = true;
        if (key == KeyEvent.VK_L) look();
        if (key == KeyEvent.VK_B) back();
        if (key == KeyEvent.VK_I) showItems();
    }

    /**
     * 判断物品类型
     * 返回: "buff" - 增益物品, "score" - 得分物品, "key" - 钥匙, "stun" - 眩晕药水
     */
    private String getItemType(Item item) {
        String name = item.getName();
        if (name.equals("生命药水") || name.equals("魔法饼干") ||
                name.equals("幽灵药水") || name.equals("加速药水")) {
            return "buff";
        } else if (name.equals("眩晕药水")) {
            return "stun";
        } else if (name.equals("钥匙")) {
            return "key";
        } else {
            return "score";
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        e.consume();
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) leftPressed = false;
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) rightPressed = false;
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) upPressed = false;
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) downPressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // ===================== 内部类 Obstacle / KeyDoor（保留原有） =====================
    class Obstacle {
        int x, y, w, h;
        boolean isSpike, solid;
        Obstacle(int x, int y, int w, int h, boolean spike, boolean solid) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.isSpike = spike;
            this.solid = solid;
        }
        void draw(Graphics2D g) {
            if (isSpike) {
                g.setColor(new Color(150, 50, 50));
                g.fillRoundRect(x, y, w, h, 5, 5);
                g.setColor(Color.RED);
                g.fillOval(x + 5, y + 5, 10, 10);
            } else {
                g.setColor(new Color(100, 70, 40));
                g.fillRoundRect(x, y, w, h, 8, 8);
                g.setColor(new Color(140, 100, 60));
                g.fillRoundRect(x + 5, y + 5, w - 10, h - 10, 5, 5);
            }
        }
        Rectangle getBounds() { return new Rectangle(x, y, w, h); }
        boolean isSpike() { return isSpike; }
    }

    class KeyDoor extends Door {
        int roomLevel;
        KeyDoor(int x, int y, int w, int h, String dir, int level) {
            super(x, y, w, h, dir);
            this.roomLevel = level;
        }
        int getRoomLevel() { return roomLevel; }
        @Override
        public void draw(Graphics2D g) {
            g.setColor(new Color(200, 180, 100, 200));
            g.fillRect(x, y, w, h);
            g.setColor(Color.BLACK);
            g.setFont(new Font("微软雅黑", Font.BOLD, 18));
            g.drawString("🔒", x + w/2 - 6, y + h/2 + 6);
        }
    }

    // ===================== 道具、障碍物分布算法（全部保留） =====================
    private List<Obstacle> generateObstaclesEvenly(int count, int minDistance,
                                                   List<Rectangle> doorAreas, int safeRadius) {
        List<Obstacle> result = new ArrayList<>();
        Rectangle playArea = getCurrentPlayArea();
        if (playArea == null || playArea.width <= 0 || playArea.height <= 0) return result;
        int gridCols = (int) Math.ceil(Math.sqrt(count * 1.5));
        int gridRows = (int) Math.ceil(count * 1.0 / gridCols);
        int cellWidth = playArea.width / gridCols;
        int cellHeight = playArea.height / gridRows;
        int obsW = 35, obsH = 35;
        List<Rectangle> forbiddenZones = new ArrayList<>();
        if (doorAreas != null) {
            for (Rectangle door : doorAreas) {
                forbiddenZones.add(new Rectangle(
                        door.x - safeRadius, door.y - safeRadius,
                        door.width + safeRadius * 2, door.height + safeRadius * 2
                ));
            }
        }
        Random rand = new Random();
        List<Point> candidates = new ArrayList<>();
        for (int row = 0; row < gridRows && result.size() < count; row++) {
            for (int col = 0; col < gridCols && result.size() < count; col++) {
                int baseX = playArea.x + col * cellWidth;
                int baseY = playArea.y + row * cellHeight;
                int offsetX = rand.nextInt(Math.max(1, cellWidth - obsW));
                int offsetY = rand.nextInt(Math.max(1, cellHeight - obsH));
                int x = Math.min(baseX + offsetX, playArea.x + playArea.width - obsW);
                int y = Math.min(baseY + offsetY, playArea.y + playArea.height - obsH);
                x = Math.max(playArea.x, x);
                y = Math.max(playArea.y, y);
                Rectangle candidate = new Rectangle(x, y, obsW, obsH);
                boolean isForbidden = false;
                for (Rectangle forbidden : forbiddenZones) {
                    if (candidate.intersects(forbidden)) {
                        isForbidden = true;
                        break;
                    }
                }
                boolean tooClose = false;
                for (Obstacle existing : result) {
                    Rectangle existingRect = existing.getBounds();
                    int dx = existingRect.x - x;
                    int dy = existingRect.y - y;
                    int distance = (int) Math.sqrt(dx * dx + dy * dy);
                    if (distance < minDistance) {
                        tooClose = true;
                        break;
                    }
                }
                if (!isForbidden && !tooClose) {
                    boolean isSpike = rand.nextInt(100) < 20;
                    result.add(new Obstacle(x, y, obsW, obsH, isSpike, true));
                } else if (candidates.size() < count * 3) {
                    candidates.add(new Point(x, y));
                }
            }
        }
        while (result.size() < count && !candidates.isEmpty()) {
            Point p = candidates.remove(rand.nextInt(candidates.size()));
            boolean isSpike = rand.nextInt(100) < 20;
            result.add(new Obstacle(p.x, p.y, obsW, obsH, isSpike, true));
        }
        return result;
    }

    private void distributeItemsEvenly(List<Item> itemsList, int minDistance,
                                       List<Rectangle> doorAreas) {
        if (itemsList == null || itemsList.isEmpty()) return;
        Rectangle playArea = getCurrentPlayArea();
        if (playArea == null || playArea.width <= 0 || playArea.height <= 0) return;
        int itemW = 20, itemH = 20;
        List<Rectangle> forbiddenZones = new ArrayList<>();
        if (doorAreas != null) {
            int safeRadius = 40;
            for (Rectangle door : doorAreas) {
                forbiddenZones.add(new Rectangle(
                        door.x - safeRadius, door.y - safeRadius,
                        door.width + safeRadius * 2, door.height + safeRadius * 2
                ));
            }
        }
        int count = itemsList.size();
        int gridCols = (int) Math.ceil(Math.sqrt(count * 1.5));
        int gridRows = (int) Math.ceil(count * 1.0 / gridCols);
        int cellWidth = playArea.width / gridCols;
        int cellHeight = playArea.height / gridRows;
        Random rand = new Random();
        List<Item> undistributed = new ArrayList<>(itemsList);
        itemsList.clear();
        for (int i = 0; i < count && !undistributed.isEmpty(); i++) {
            int row = i / gridCols;
            int col = i % gridCols;
            if (row >= gridRows) break;
            int baseX = playArea.x + col * cellWidth + cellWidth / 2 - itemW / 2;
            int baseY = playArea.y + row * cellHeight + cellHeight / 2 - itemH / 2;
            int maxOffset = Math.min(cellWidth / 3, 40);
            int offsetX = rand.nextInt(maxOffset * 2 + 1) - maxOffset;
            int offsetY = rand.nextInt(maxOffset * 2 + 1) - maxOffset;
            int x = baseX + offsetX;
            int y = baseY + offsetY;
            x = Math.max(playArea.x, Math.min(playArea.x + playArea.width - itemW, x));
            y = Math.max(playArea.y, Math.min(playArea.y + playArea.height - itemH, y));
            boolean isForbidden = false;
            Rectangle itemRect = new Rectangle(x, y, itemW, itemH);
            for (Rectangle forbidden : forbiddenZones) {
                if (itemRect.intersects(forbidden)) {
                    isForbidden = true;
                    break;
                }
            }
            if (!isForbidden) {
                Item item = undistributed.remove(0);
                setItemPosition(item, x, y);
                itemsList.add(item);
            } else {
                undistributed.add(undistributed.remove(0));
            }
        }
        RandomPositions: for (Item item : undistributed) {
            for (int attempt = 0; attempt < 50; attempt++) {
                int x = playArea.x + rand.nextInt(playArea.width - itemW);
                int y = playArea.y + rand.nextInt(playArea.height - itemH);
                boolean isForbidden = false;
                Rectangle itemRect = new Rectangle(x, y, itemW, itemH);
                for (Rectangle forbidden : forbiddenZones) {
                    if (itemRect.intersects(forbidden)) {
                        isForbidden = true;
                        break;
                    }
                }
                if (!isForbidden) {
                    setItemPosition(item, x, y);
                    itemsList.add(item);
                    continue RandomPositions;
                }
            }
            setItemPosition(item, playArea.x + 100, playArea.y + 100);
            itemsList.add(item);
        }
    }

    private void setItemPosition(Item item, int x, int y) {
        try {
            java.lang.reflect.Field fieldX = Item.class.getDeclaredField("x");
            java.lang.reflect.Field fieldY = Item.class.getDeclaredField("y");
            fieldX.setAccessible(true);
            fieldY.setAccessible(true);
            fieldX.set(item, x);
            fieldY.set(item, y);
        } catch (Exception e) {}
    }

    private Rectangle getCurrentPlayArea() {
        int roomX = 10;
        int roomY = 10;
        int roomW = gameCanvas.getWidth() - 20;
        int roomH = gameCanvas.getHeight() - 20;
        return new Rectangle(roomX, roomY, roomW, roomH);
    }

    private List<Rectangle> getAllDoorAreas() {
        List<Rectangle> areas = new ArrayList<>();
        if (doors != null) {
            for (Door door : doors) {
                areas.add(door.getBounds());
            }
        }
        if (keyDoors != null) {
            for (KeyDoor kd : keyDoors) {
                if (kd.getRoomLevel() == currentRoomIndex + 1) {
                    areas.add(kd.getBounds());
                }
            }
        }
        return areas;
    }

    private void regenerateObstacles() {
        List<Rectangle> doorAreas = getAllDoorAreas();
        obstacles = generateObstaclesEvenly(16, 50, doorAreas, 60);
    }

    private void redistributeItems() {
        if (items == null || items.isEmpty()) return;
        List<Rectangle> doorAreas = getAllDoorAreas();
        distributeItemsEvenly(items, 40, doorAreas);
    }
}