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

public class GamePanel extends JPanel implements ActionListener, KeyListener {

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

    // UI 面板宽度
    private final int LEFT_PANEL_WIDTH = 180;
    private final int RIGHT_PANEL_WIDTH = 200;
    private final int BOTTOM_PANEL_HEIGHT = 30;

    public GamePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 30));
        setFocusable(true);
        addKeyListener(this);

        random = new Random();
        initGame();
        loadPlayerImage();

        timer = new Timer(1000 / 60, this);
        timer.start();
        requestFocusInWindow();
    }

    private void initGame() {
        initRooms();
        initObstacles();
        initDoors();
        initKeyDoors();

        currentRoomIndex = 0;
        levelCompleted = false;
        message = "";
        messageTimer = 0;
        loadCurrentRoom();

        player = new Player(400, 400);
        health = maxHealth;
        score = 0;
        hasKey = false;
        ghostMode = false;
        ghostModeTimer = 0;
    }

    private void initRooms() {
        rooms = new ArrayList<>();
        Room room1 = new Room("森林", new Color(30, 60, 30), 1);
        room1.addItem(new Item("金色徽章", "闪亮的徽章", 2, 100, 100));
        room1.addItem(new Item("魔法水晶", "紫色水晶", 3, 700, 150));
        room1.addItem(new Item("生命药水", "恢复20生命", 1, 300, 550));
        room1.addItem(new Item("魔法饼干", "增加负重上限", 1, 650, 500));
        room1.addItem(new Item("幽灵药水", "5秒内穿过障碍物", 1, 500, 400));
        room1.addItem(new Item("钥匙", "打开锁着的门", 1, 200, 300));
        rooms.add(room1);

        Room room2 = new Room("洞穴", new Color(60, 50, 40), 2);
        room2.addItem(new Item("冰晶碎片", "散发寒气", 2, 120, 180));
        room2.addItem(new Item("霜之精华", "冰霜精华", 3, 680, 220));
        room2.addItem(new Item("寒冰宝石", "珍贵宝石", 4, 550, 500));
        room2.addItem(new Item("生命药水", "恢复20生命", 1, 400, 80));
        room2.addItem(new Item("幽灵药水", "5秒内穿过障碍物", 1, 300, 450));
        rooms.add(room2);

        Room room3 = new Room("深渊", new Color(80, 30, 20), 3);
        room3.addItem(new Item("火焰符文", "火焰符文", 2, 100, 250));
        room3.addItem(new Item("熔岩之心", "滚烫核心", 5, 700, 350));
        room3.addItem(new Item("凤凰羽毛", "传说羽毛", 1, 400, 550));
        room3.addItem(new Item("魔法饼干", "增加负重上限", 1, 250, 100));
        room3.addItem(new Item("钥匙", "打开锁着的门", 1, 600, 500));
        rooms.add(room3);
    }

    private void initObstacles() {
        obstacles = new ArrayList<>();
        // 柱子
        obstacles.add(new Obstacle(80, 80, 40, 40, false, true));
        obstacles.add(new Obstacle(750, 80, 40, 40, false, true));
        obstacles.add(new Obstacle(80, 520, 40, 40, false, true));
        obstacles.add(new Obstacle(750, 520, 40, 40, false, true));
        obstacles.add(new Obstacle(400, 150, 40, 40, false, true));
        obstacles.add(new Obstacle(600, 400, 40, 40, false, true));
        obstacles.add(new Obstacle(200, 450, 40, 40, false, true));
        obstacles.add(new Obstacle(500, 250, 40, 40, false, true));
        // 尖刺
        obstacles.add(new Obstacle(300, 100, 30, 30, true, true));
        obstacles.add(new Obstacle(650, 300, 30, 30, true, true));
        obstacles.add(new Obstacle(150, 500, 30, 30, true, true));
        obstacles.add(new Obstacle(550, 550, 30, 30, true, true));
        obstacles.add(new Obstacle(450, 350, 30, 30, true, true));
        obstacles.add(new Obstacle(250, 200, 30, 30, true, true));
    }

    private void initDoors() {
        doors = new ArrayList<>();
        doors.add(new Door(20, 300, 30, 50, "west"));
        doors.add(new Door(750, 300, 30, 50, "east"));
        doors.add(new Door(400, 20, 50, 30, "north"));
        doors.add(new Door(400, 620, 50, 30, "south"));
    }

    private void initKeyDoors() {
        keyDoors = new ArrayList<>();
        keyDoors.add(new KeyDoor(700, 250, 40, 60, "east", 1));
        keyDoors.add(new KeyDoor(100, 550, 40, 60, "south", 2));
        keyDoors.add(new KeyDoor(300, 20, 60, 40, "north", 3));
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
        playerX = 400;
        playerY = 400;
        if (player != null) player.setPosition(400, 400);
        levelCompleted = false;
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
        showMessage("进入 " + rooms.get(currentRoomIndex).getName() + "！", 60);
    }

    private void gameOver() {
        showMessage("🎉 通关胜利！总分数: " + score + " 🎉", 180);
        levelCompleted = true;
    }

    private void showMessage(String msg, int duration) {
        message = msg;
        messageTimer = duration;
    }

    private void checkObstacleCollision() {
        if (ghostMode) return;
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        for (Obstacle obs : obstacles) {
            if (playerRect.intersects(obs.getBounds())) {
                // 阻挡移动
                if (leftPressed) playerX += 5;
                if (rightPressed) playerX -= 5;
                if (upPressed) playerY += 5;
                if (downPressed) playerY -= 5;
                while (playerRect.intersects(obs.getBounds())) {
                    if (leftPressed) playerX++;
                    if (rightPressed) playerX--;
                    if (upPressed) playerY++;
                    if (downPressed) playerY--;
                    playerRect.setBounds(playerX, playerY, playerWidth, playerHeight);
                }
                if (obs.isSpike()) {
                    health -= 10;
                    if (health <= 0) {
                        health = 0;
                        restartGame();
                        showMessage("你死了！游戏重新开始！", 60);
                    } else {
                        showMessage("触碰尖刺！-10生命！", 30);
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
        } else if (name.equals("生命药水")) {
            health = Math.min(maxHealth, health + 20);
            items.remove(nearbyItem);
            showMessage("生命+20！当前 " + health + "/" + maxHealth, 60);
        } else if (name.equals("幽灵药水")) {
            ghostMode = true;
            ghostModeTimer = 300;
            items.remove(nearbyItem);
            showMessage("幽灵模式！5秒内可穿过障碍物！", 60);
        } else if (name.equals("钥匙")) {
            hasKey = true;
            items.remove(nearbyItem);
            showMessage("获得了钥匙！可以打开锁着的门！", 60);
        } else {
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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (levelCompleted) {
            repaint();
            return;
        }
        if (leftPressed) playerX -= speed;
        if (rightPressed) playerX += speed;
        if (upPressed) playerY -= speed;
        if (downPressed) playerY += speed;

        // 玩家移动边界：不能进入左右UI面板区域
        int minX = LEFT_PANEL_WIDTH + 10;
        int maxX = getWidth() - RIGHT_PANEL_WIDTH - playerWidth - 10;
        int minY = 10;
        int maxY = getHeight() - BOTTOM_PANEL_HEIGHT - playerHeight - 10;
        playerX = Math.max(minX, Math.min(maxX, playerX));
        playerY = Math.max(minY, Math.min(maxY, playerY));

        if (player != null) player.setPosition(playerX, playerY);

        updatePowerUpTimers();
        if (ghostModeTimer > 0) {
            ghostModeTimer--;
            if (ghostModeTimer <= 0) ghostMode = false;
        }
        checkObstacleCollision();
        checkDoors();
        checkKeyDoors();
        checkNearbyItem();

        if (spacePressed) {
            pickupNearbyItem();
            spacePressed = false;
        }

        if (messageTimer > 0) messageTimer--;
        repaint();
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

    private void drawGridBackground(Graphics2D g) {
        g.setColor(new Color(30, 35, 45));
        g.fillRect(0, 0, getWidth(), getHeight());
        int cell = 40;
        g.setColor(new Color(50, 55, 65));
        for (int x = 0; x < getWidth(); x += cell) g.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += cell) g.drawLine(0, y, getWidth(), y);
    }

    private void drawRoomArea(Graphics2D g) {
        // 房间区域在左右面板之间，留出边距
        int roomX = LEFT_PANEL_WIDTH + 10;
        int roomY = 10;
        int roomW = getWidth() - LEFT_PANEL_WIDTH - RIGHT_PANEL_WIDTH - 20;
        int roomH = getHeight() - BOTTOM_PANEL_HEIGHT - 20;
        Room currentRoom = rooms.get(currentRoomIndex);
        g.setColor(currentRoom.getBackgroundColor());
        g.fillRoundRect(roomX, roomY, roomW, roomH, 20, 20);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(4));
        g.drawRoundRect(roomX, roomY, roomW, roomH, 20, 20);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        drawGridBackground(g2d);
        drawRoomArea(g2d);

        // 绘制障碍物、门、物品、玩家等（使用绝对坐标，它们已经在房间区域内）
        for (Obstacle obs : obstacles) obs.draw(g2d);
        for (Door door : doors) door.draw(g2d);
        for (KeyDoor kd : keyDoors) {
            if (kd.getRoomLevel() == currentRoomIndex + 1) kd.draw(g2d);
        }
        for (Item item : items) {
            drawItem(g2d, item.getX(), item.getY());
            g2d.setColor(new Color(255, 220, 150));
            g2d.setFont(new Font("微软雅黑", Font.PLAIN, 10));
            g2d.drawString(item.getName(), item.getX() - 5, item.getY() - 5);
        }

        if (playerImage != null) {
            g2d.drawImage(playerImage, playerX, playerY, playerWidth, playerHeight, null);
        } else {
            g2d.setColor(new Color(70, 130, 200));
            g2d.fillRoundRect(playerX, playerY, playerWidth, playerHeight, 8, 8);
        }

        if (nearbyItem != null) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRoundRect(playerX - 10, playerY - 25, 70, 22, 8, 8);
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 11));
            g2d.drawString("空格拾取", playerX - 5, playerY - 10);
        }

        drawLeftPanel(g2d);
        drawRightPanel(g2d);
        drawBottomBar(g2d);

        if (inventoryUIVisible) drawInventoryUI(g2d);
        if (messageTimer > 0 && !message.isEmpty()) drawMessage(g2d);
        if (levelCompleted && currentRoomIndex + 1 >= rooms.size()) drawGameOver(g2d);
    }

    private void drawLeftPanel(Graphics2D g) {
        int x = 10;
        int y = 10;
        int w = LEFT_PANEL_WIDTH - 10;
        int h = getHeight() - BOTTOM_PANEL_HEIGHT - 20;
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(x, y, w, h, 12, 12);
        g.setColor(new Color(100, 180, 250));
        g.drawRoundRect(x, y, w, h, 12, 12);

        int py = y + 20;
        g.setFont(new Font("微软雅黑", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("冒险者", x + 10, py);
        py += 20;
        // 生命条
        g.setColor(new Color(80, 0, 0));
        g.fillRoundRect(x + 10, py, w - 20, 14, 7, 7);
        int hpPercent = health * (w - 20) / maxHealth;
        g.setColor(new Color(220, 50, 50));
        g.fillRoundRect(x + 10, py, hpPercent, 14, 7, 7);
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.BOLD, 10));
        g.drawString(health + "/" + maxHealth, x + w/2 - 20, py + 11);
        py += 20;
        // 分数
        g.setFont(new Font("微软雅黑", Font.BOLD, 14));
        g.setColor(Color.YELLOW);
        g.drawString("💰 " + score, x + 10, py);
        py += 20;
        // 负重条
        g.setColor(new Color(60, 40, 40));
        g.fillRoundRect(x + 10, py, w - 20, 12, 6, 6);
        int weightPercent = player.getCurrentWeight() * (w - 20) / player.getMaxWeight();
        g.setColor(new Color(255, 180, 50));
        g.fillRoundRect(x + 10, py, weightPercent, 12, 6, 6);
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 9));
        g.drawString("负重:" + player.getCurrentWeight() + "/" + player.getMaxWeight() + "kg", x + 10, py + 18);
        py += 30;
        // 状态
        g.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        if (speedBoostTimer > 0) { g.setColor(Color.GREEN); g.drawString("⚡加速中", x + 10, py); py += 18; }
        if (ghostMode) { g.setColor(Color.MAGENTA); g.drawString("👻幽灵模式", x + 10, py); py += 18; }
        if (hasKey) { g.setColor(Color.YELLOW); g.drawString("🔑持有钥匙", x + 10, py); }
    }

    private void drawRightPanel(Graphics2D g) {
        int x = getWidth() - RIGHT_PANEL_WIDTH + 10;
        int y = 10;
        int w = RIGHT_PANEL_WIDTH - 20;
        int h = getHeight() - BOTTOM_PANEL_HEIGHT - 20;
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(x, y, w, h, 12, 12);
        g.setColor(new Color(100, 180, 250));
        g.drawRoundRect(x, y, w, h, 12, 12);

        Room currentRoom = rooms.get(currentRoomIndex);
        g.setFont(new Font("微软雅黑", Font.BOLD, 14));
        g.setColor(Color.CYAN);
        g.drawString("🏠 " + currentRoom.getName(), x + 10, y + 25);
        g.setFont(new Font("微软雅黑", Font.BOLD, 12));
        g.setColor(Color.YELLOW);
        g.drawString("📦 物品清单 (" + items.size() + ")", x + 10, y + 50);

        int py = y + 70;
        g.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        for (Item item : items) {
            if (py > y + h - 60) break;
            g.setColor(new Color(255, 220, 150));
            g.drawString("● " + item.getName() + " (" + item.getWeight() + "kg)", x + 10, py);
            py += 18;
        }
        py += 10;
        g.setColor(Color.CYAN);
        g.drawString("🚪 房门出口", x + 10, py);
        py += 18;
        g.setColor(Color.WHITE);
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
                    case "north": arrow = "↑ 北(🔒)"; break;
                    case "south": arrow = "↓ 南(🔒)"; break;
                    case "east": arrow = "→ 东(🔒)"; break;
                    case "west": arrow = "← 西(🔒)"; break;
                }
                g.setColor(Color.RED);
                g.drawString(arrow, x + 10, py);
                py += 16;
            }
        }
    }

    private void drawBottomBar(Graphics2D g) {
        int y = getHeight() - BOTTOM_PANEL_HEIGHT;
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(0, y, getWidth(), BOTTOM_PANEL_HEIGHT, 10, 10);
        g.setColor(new Color(200, 200, 220));
        g.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        String tips = "WASD/方向键 移动   空格 拾取   I 背包   L 查看房间   B 返回上一房间   R 重新开始";
        int tipW = g.getFontMetrics().stringWidth(tips);
        g.drawString(tips, (getWidth() - tipW) / 2, y + 20);
    }

    private void drawInventoryUI(Graphics2D g) {
        if (!inventoryUIVisible) return;
        g.setColor(new Color(0, 0, 0, 220));
        g.fillRect(0, 0, getWidth(), getHeight());

        int pw = 500, ph = 400;
        int px = (getWidth() - pw) / 2;
        int py = (getHeight() - ph) / 2;
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

    private void drawMessage(Graphics2D g) {
        int w = 500, h = 50;
        int x = (getWidth() - w) / 2;
        int y = (getHeight() - h) / 2;
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(x, y, w, h, 15, 15);
        g.setFont(new Font("微软雅黑", Font.BOLD, 16));
        g.setColor(new Color(255, 220, 100));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(message, x + (w - fm.stringWidth(message)) / 2, y + 32);
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 220));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setFont(new Font("微软雅黑", Font.BOLD, 48));
        g.setColor(Color.YELLOW);
        String msg = "🎉 通关胜利！";
        g.drawString(msg, (getWidth() - g.getFontMetrics().stringWidth(msg)) / 2, getHeight() / 2 - 50);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 24));
        g.setColor(Color.WHITE);
        String scoreMsg = "最终得分: " + score;
        g.drawString(scoreMsg, (getWidth() - g.getFontMetrics().stringWidth(scoreMsg)) / 2, getHeight() / 2 + 20);
        String restart = "按 R 键重新开始";
        g.drawString(restart, (getWidth() - g.getFontMetrics().stringWidth(restart)) / 2, getHeight() / 2 + 80);
    }

    private void drawItem(Graphics2D g, int x, int y) {
        g.setColor(new Color(255, 200, 0));
        g.fillOval(x, y, 18, 18);
        g.setColor(new Color(255, 255, 100));
        g.fillOval(x + 4, y + 4, 10, 10);
    }

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
            showMessage("返回上一个房间：" + rooms.get(currentRoomIndex).getName(), 60);
            playerX = 400;
            playerY = 400;
            if (player != null) player.setPosition(400, 400);
        } else {
            showMessage("已经在第一个房间，无法返回！", 60);
        }
    }

    private void showItems() {
        inventoryUIVisible = !inventoryUIVisible;
    }

    private void restartGame() {
        currentRoomIndex = 0;
        score = 0;
        health = maxHealth;
        levelCompleted = false;
        roomStateMap.clear();
        hasKey = false;
        ghostMode = false;
        ghostModeTimer = 0;
        loadCurrentRoom();
        player = new Player(400, 400);
        showMessage("游戏重新开始！", 60);
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
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

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) leftPressed = false;
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) rightPressed = false;
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) upPressed = false;
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) downPressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // ==================== 内部类 ====================
    class Obstacle {
        int x, y, w, h;
        boolean isSpike, solid;
        Obstacle(int x, int y, int w, int h, boolean spike, boolean solid) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.isSpike = spike; this.solid = solid;
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
}