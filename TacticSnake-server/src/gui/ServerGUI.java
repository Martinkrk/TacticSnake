package gui;

import com.shared.game.Game;
import game.OnlineGame;
import server.Server;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class ServerGUI extends JFrame {
private Server server;
    private JTabbedPane tabbedPane;
    private JPanel logsPanel, gamesPanel, statsPanel;
    private JTextArea logsTextArea, statsTextArea;
    private MemoryUsagePanel memoryUsagePanel;
    private GamesPanel gamesTextArea;

    public ServerGUI(Server server) {
        // Set the title and size of the window
        super("Server GUI");
        setSize(900, 600);

        this.server = server;

        // Create a JTabbedPane with 3 tabs
        tabbedPane = new JTabbedPane();
        logsPanel = new JPanel();
        gamesPanel = new GamesTab(new ArrayList<>());
        statsPanel = new JPanel();
        tabbedPane.addTab("Logs", logsPanel);
        tabbedPane.addTab("Games", gamesPanel);
        tabbedPane.addTab("Statistics", statsPanel);

        logsPanel.setLayout(new BorderLayout());
        // Create a JPanel to hold the MemoryUsagePanel
        memoryUsagePanel = new MemoryUsagePanel();
        JPanel memoryPanel = new JPanel();
        memoryPanel.setLayout(new BorderLayout());
        memoryPanel.add(memoryUsagePanel, BorderLayout.WEST);
        logsPanel.add(memoryPanel, BorderLayout.WEST);

        // Create a JTextArea to hold the logs
        logsTextArea = new JTextArea();
        logsTextArea.setEditable(false);
        JScrollPane logsScrollPane = new JScrollPane(logsTextArea); // Wrap the logsTextArea in a JScrollPane
        logsPanel.add(logsScrollPane, BorderLayout.CENTER); // Add the JScrollPane instead of the logsTextArea

        statsTextArea = new JTextArea(20, 70);
        statsTextArea.setEditable(false);
        statsPanel.add(new JScrollPane(statsTextArea));

        // Add the JTabbedPane to the window
        add(tabbedPane);

        // Make the window visible
        setVisible(true);

        //On window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                server.stopServer();
            }
        });
    }

    public void appendLog(String message) {
        logsTextArea.append(message + "\n");
    }

    public void updateGames(List<OnlineGame> games) {
        gamesPanel = new GamesTab(games);
        tabbedPane.setComponentAt(1, gamesPanel);
    }

    public Server getServer() {
        return server;
    }
}
