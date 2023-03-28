package gui;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import javax.swing.*;

public class MemoryUsagePanel extends JPanel {
    private JLabel heapLabel, nonHeapLabel;
    private MemoryMXBean memoryMXBean;

    public MemoryUsagePanel() {
        // Set the layout and border of the panel
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Get the MemoryMXBean instance to retrieve the memory usage information
        memoryMXBean = ManagementFactory.getMemoryMXBean();

        // Create two JLabels to display the heap and non-heap memory usage
        heapLabel = new JLabel();
        nonHeapLabel = new JLabel();

        // Add the labels to the panel
        add(new JLabel("Heap Memory Usage:"));
        add(heapLabel);
        add(new JLabel("Non-Heap Memory Usage:"));
        add(nonHeapLabel);

        // Update the memory usage labels every second
        new Thread(() -> {
            while (true) {
                MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
                MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
                heapLabel.setText(String.format("<html><pre>%-20s %10s<br>%-20s %10s<br>%-20s %10s</pre></html>",
                        "Initial Memory:", formatSize(heapMemoryUsage.getInit()),
                        "Used Memory:", formatSize(heapMemoryUsage.getUsed()),
                        "Max Memory:", formatSize(heapMemoryUsage.getMax())));
                nonHeapLabel.setText(String.format("<html><pre>%-20s %10s<br>%-20s %10s<br>%-20s %10s</pre></html>",
                        "Initial Memory:", formatSize(nonHeapMemoryUsage.getInit()),
                        "Used Memory:", formatSize(nonHeapMemoryUsage.getUsed()),
                        "Max Memory:", formatSize(nonHeapMemoryUsage.getMax())));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String formatSize(long bytes) {
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int index = 0;
        double size = bytes;
        while (size >= 1024 && index < units.length - 1) {
            size /= 1024;
            index++;
        }
        return String.format("%.2f %s", size, units[index]);
    }

}