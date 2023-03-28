package gui;

import javax.swing.*;
import java.awt.*;

public class GamesPanel extends JPanel {
    private JPanel gameList;

    public GamesPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel headerLabel = new JLabel("Game List");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(headerLabel, BorderLayout.NORTH);

        gameList = new JPanel();
        gameList.setLayout(new GridLayout(0, 2, 10, 10));
        JScrollPane scrollPane = new JScrollPane(gameList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        addGame("Game 1", "Player 1", "Player 2", "Player 3");
        addGame("Game 2", "Player 1", "Player 2");
        addGame("Game 3");

        setPreferredSize(new Dimension(600, 400));
    }

    private void addGame(String gameName, String... playerNames) {
        JPanel gamePanel = new JPanel();
        gamePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));
        JLabel gameLabel = new JLabel(gameName);
        gamePanel.add(gameLabel);
        if (playerNames.length > 0) {
            JPanel playerListPanel = new JPanel();
            playerListPanel.setLayout(new BoxLayout(playerListPanel, BoxLayout.Y_AXIS));
            for (String playerName : playerNames) {
                JLabel playerLabel = new JLabel(playerName);
                playerListPanel.add(playerLabel);
            }
            gamePanel.add(playerListPanel);
        }   
        gameList.add(gamePanel);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Game List Panel Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new GamesPanel());
        frame.pack();
        frame.setVisible(true);
    }
}
