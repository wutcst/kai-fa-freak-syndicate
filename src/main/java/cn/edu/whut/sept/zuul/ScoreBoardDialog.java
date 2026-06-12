package cn.edu.whut.sept.zuul;


import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * 排行榜对话框
 */
public class ScoreBoardDialog extends JDialog {

    public ScoreBoardDialog(JFrame parent) {
        super(parent, "排行榜", true);
        initComponents();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void initComponents() {
        setSize(500, 400);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 35, 45));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 标题
        JLabel titleLabel = new JLabel("🏆 游戏排行榜 🏆", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(255, 200, 100));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 表格数据
        UserManager um = UserManager.getInstance();
        List<User> users = um.getAllUsers();

        String[] columns = {"排名", "玩家", "最高分", "游戏次数", "平均分"};
        Object[][] data = new Object[users.size()][5];

        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            data[i][0] = i + 1;
            data[i][1] = u.getUsername();
            data[i][2] = u.getHighestScore();
            data[i][3] = u.getTotalGames();
            data[i][4] = String.format("%.1f", u.getAverageScore());
        }

        JTable table = new JTable(data, columns);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.setBackground(new Color(50, 55, 65));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(80, 85, 95));

        // 设置表头样式
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("微软雅黑", Font.BOLD, 13));
        header.setBackground(new Color(70, 130, 200));
        header.setForeground(Color.WHITE);

        // 设置列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 200), 2));
        scrollPane.getViewport().setBackground(new Color(50, 55, 65));

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 关闭按钮
        JButton closeButton = new JButton("关闭");
        closeButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        closeButton.setBackground(new Color(70, 130, 200));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }
}