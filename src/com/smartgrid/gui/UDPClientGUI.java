package com.smartgrid.gui;

import com.smartgrid.udp.UDPClient;
import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class UDPClientGUI extends JFrame {
    private JButton btnStart;
    private JButton btnClear;
    private JTextArea txtStatus;
    private JTextField txtServerIP;
    private JSpinner spinDelay;
    private JLabel lblRecordCount;
    private JLabel lblStatus;
    private Set<String> sentTimestamps;
    
    public UDPClientGUI() {
        setTitle("UDP Client - Live Energy Stream (Dataset 2)");
        setSize(720, 600);  // 20% larger (was 600x500)
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        sentTimestamps = new HashSet<>();
        
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Connection"));
        topPanel.add(new JLabel("Server IP:"));
        txtServerIP = new JTextField("localhost", 14);
        topPanel.add(txtServerIP);
        
        topPanel.add(new JLabel("Delay (ms):"));
        spinDelay = new JSpinner(new SpinnerNumberModel(1000, 100, 5000, 100));
        topPanel.add(spinDelay);
        
        btnStart = new JButton("Start UDP Live Stream");
        btnStart.setBackground(new Color(50, 200, 100));
        btnStart.setForeground(Color.WHITE);
        btnStart.setFont(new Font("Arial", Font.BOLD, 12));
        topPanel.add(btnStart);
        
        btnClear = new JButton("Clear Log");
        btnClear.setBackground(Color.LIGHT_GRAY);
        topPanel.add(btnClear);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        JTextArea infoArea = new JTextArea();
        infoArea.setText("DATASET 2: Smart Home Energy Usage\n");
        infoArea.append("Records: ~400 rows\n");
        infoArea.append("Key field: Total_Energy_kWh\n");
        infoArea.append("Server detects: Threshold > 4.0 kWh  |  Spike > 2x previous\n");
        infoArea.append("Data Validation: Missing values filtered, duplicate timestamps removed\n");
        infoArea.setEditable(false);
        infoArea.setBackground(new Color(240, 255, 240));
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        centerPanel.add(infoArea, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        txtStatus = new JTextArea(14, 45);
        txtStatus.setEditable(false);
        txtStatus.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(txtStatus);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Live Stream Status"));
        
        JPanel statusBar = new JPanel(new BorderLayout());
        lblRecordCount = new JLabel("Records sent: 0 (duplicates filtered: 0)");
        lblStatus = new JLabel("Ready");
        lblStatus.setForeground(Color.GRAY);
        statusBar.add(lblRecordCount, BorderLayout.WEST);
        statusBar.add(lblStatus, BorderLayout.EAST);
        
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        bottomPanel.add(statusBar, BorderLayout.SOUTH);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        btnStart.addActionListener(e -> startStream());
        btnClear.addActionListener(e -> txtStatus.setText(""));
    }
    
    private void startStream() {
        btnStart.setEnabled(false);
        sentTimestamps.clear();
        txtStatus.append("=== UDP LIVE STREAM STARTED ===\n");
        txtStatus.append("Server: " + txtServerIP.getText() + ":9876\n");
        txtStatus.append("Data Validation: Duplicate filtering ENABLED\n");
        txtStatus.append("------------------------------------\n\n");
        
        lblStatus.setText("Streaming...");
        lblStatus.setForeground(Color.ORANGE);
        
        new Thread(() -> {
            try {
                int delay = (Integer) spinDelay.getValue();
                UDPClient client = new UDPClient(txtServerIP.getText(), delay);
                
                PrintStream ps = new PrintStream(new TextAreaOutputStream(txtStatus, lblRecordCount, lblStatus, sentTimestamps));
                System.setOut(ps);
                
                client.sendLiveFeed("data/smart_home_energy_usage_dataset.csv");
                
                SwingUtilities.invokeLater(() -> {
                    txtStatus.append("\n=== STREAM COMPLETE ===\n");
                    lblStatus.setText("Complete");
                    lblStatus.setForeground(Color.GREEN);
                    btnStart.setEnabled(true);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    txtStatus.append("\nERROR: " + e.getMessage() + "\n");
                    lblStatus.setText("Error");
                    lblStatus.setForeground(Color.RED);
                    btnStart.setEnabled(true);
                });
            }
        }).start();
    }
    
    static class TextAreaOutputStream extends java.io.OutputStream {
        private JTextArea textArea;
        private JLabel recordLabel;
        private JLabel statusLabel;
        private Set<String> sentTimestamps;
        private StringBuilder buffer = new StringBuilder();
        private int duplicateCount = 0;
        
        public TextAreaOutputStream(JTextArea textArea, JLabel recordLabel, JLabel statusLabel, Set<String> sentTimestamps) {
            this.textArea = textArea;
            this.recordLabel = recordLabel;
            this.statusLabel = statusLabel;
            this.sentTimestamps = sentTimestamps;
        }
        
        @Override
        public void write(int b) {
            char c = (char) b;
            buffer.append(c);
            if (c == '\n') {
                final String line = buffer.toString();
                SwingUtilities.invokeLater(() -> {
                    textArea.append(line);
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                    
                    if (line.contains("[") && line.contains("]") && line.contains("Energy:")) {
                        try {
                            String num = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                            recordLabel.setText("Records sent: " + num + " (duplicates filtered: " + duplicateCount + ")");
                            statusLabel.setText("Streaming record " + num);
                            statusLabel.setForeground(Color.BLUE);
                        } catch (Exception e) {}
                    }
                });
                buffer.setLength(0);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UDPClientGUI().setVisible(true));
    }
}