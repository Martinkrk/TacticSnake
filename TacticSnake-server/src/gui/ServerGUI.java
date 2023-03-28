package gui;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.*;

public class ServerGUI extends JFrame {

    private JTabbedPane tabbedPane;
    private JPanel logsPanel, gamesPanel, statsPanel;
    private JTextArea logsTextArea, statsTextArea;
    private MemoryUsagePanel memoryUsagePanel;
    private GamesPanel gamesTextArea;

    public ServerGUI() {
        // Set the title and size of the window
        super("Server GUI");
        setSize(800, 600);

        // Create a JTabbedPane with 3 tabs
        tabbedPane = new JTabbedPane();
        logsPanel = new JPanel();
        gamesPanel = new JPanel();
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

        gamesTextArea = new GamesPanel();
        gamesPanel.add(new JScrollPane(gamesTextArea));

        statsTextArea = new JTextArea(20, 70);
        statsTextArea.setEditable(false);
        statsPanel.add(new JScrollPane(statsTextArea));

        // Add the JTabbedPane to the window
        add(tabbedPane);

        // Make the window visible
        setVisible(true);
    }

    public void appendLog(String message) {
        logsTextArea.append(message + "\n");
    }
}
