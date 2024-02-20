package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MemoryUsagePanel extends JPanel implements Runnable {
    private static final long serialVersionUID = 1L;
    private JLabel usedLabel;
    private JLabel freeLabel;
    private JLabel maxLabel;
    private JLabel currentLabel;
    private double maxMemory;
    private double currentMemory;

    public MemoryUsagePanel() {
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        setLayout(new BorderLayout());

        JPanel labelsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets.left = 5;
        gbc.insets.bottom = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;

        JLabel usedTitleLabel = new JLabel("Used:");
        usedLabel = new JLabel("0.0 MB");
        usedLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.gridy++;
        labelsPanel.add(usedTitleLabel, gbc);
        gbc.gridx++;
        labelsPanel.add(usedLabel, gbc);

        JLabel freeTitleLabel = new JLabel("Free:");
        freeLabel = new JLabel("0.0 MB");
        freeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.gridx = 0;
        gbc.gridy++;
        labelsPanel.add(freeTitleLabel, gbc);
        gbc.gridx++;
        labelsPanel.add(freeLabel, gbc);

        JLabel maxTitleLabel = new JLabel("Max:");
        maxLabel = new JLabel("0.0 MB");
        maxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.gridx = 0;
        gbc.gridy++;
        labelsPanel.add(maxTitleLabel, gbc);
        gbc.gridx++;
        labelsPanel.add(maxLabel, gbc);

        JLabel currentTitleLabel = new JLabel("Current:");
        currentLabel = new JLabel("0.0 MB");
        currentLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.gridx = 0;
        gbc.gridy++;
        labelsPanel.add(currentTitleLabel, gbc);
        gbc.gridx++;
        labelsPanel.add(currentLabel, gbc);

        add(labelsPanel, BorderLayout.CENTER);

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage usage = memoryBean.getHeapMemoryUsage();
        maxMemory = usage.getMax() / (1024.0 * 1024.0);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        while (true) {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage usage = memoryBean.getHeapMemoryUsage();
            double usedMemory = usage.getUsed() / (1024.0 * 1024.0);
            double freeMemory = (usage.getMax() - usage.getUsed()) / (1024.0 * 1024.0);
            currentMemory = usage.getCommitted() / (1024.0 * 1024.0);

            usedLabel.setText(String.format("%.1f MB", usedMemory));
            freeLabel.setText(String.format("%.1f MB", freeMemory));
            currentLabel.setText(String.format("%.1f MB", currentMemory));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            repaint();
        }
    }
}