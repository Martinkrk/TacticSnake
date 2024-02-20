package gui;

import com.shared.player.BotLogic;
import com.shared.player.Snake;
import game.OnlineGame;
import game.OnlinePlayer;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GamesTab extends JPanel {
    private JPanel cardPanel; //TODO Remove and test
    public GamesTab(List<OnlineGame> games) {
        cardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cardPanel.setPreferredSize(new Dimension(1000, 50*50));

        for (OnlineGame game : games) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            card.setPreferredSize(new Dimension(260, 120));

            JLabel titleLabel = new JLabel("GAME: " + game.getGameId());
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            titleLabel.setHorizontalAlignment(JLabel.CENTER);
            card.add(titleLabel, BorderLayout.NORTH);

            JPanel infoPanel = new JPanel(new GridLayout(4, 1));

            for (Snake player : game.getPlayers()) {
                JLabel playerLabel;
                if (player instanceof BotLogic) {
                    playerLabel = new JLabel("BOT");
                } else {
                    playerLabel = new JLabel(String.format("(%s) - %s",
                            ((OnlinePlayer) player).getSocket().getInetAddress(), player.getNick()));
                }
                playerLabel.setHorizontalAlignment(JLabel.CENTER);
                infoPanel.add(playerLabel);
            }
            card.add(infoPanel, BorderLayout.CENTER);
            cardPanel.add(card);
        }

        JScrollPane scrollPane = new JScrollPane(cardPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }
}
