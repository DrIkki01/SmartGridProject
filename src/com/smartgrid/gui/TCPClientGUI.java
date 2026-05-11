package com.smartgrid.gui;

import com.smartgrid.tcp.TCPClient;
import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class TCPClientGUI extends JFrame {
    private JButton btnSend;
    private JButton btnClear;
    private JTextArea txtStatus;
    private JTextField txtServerIP;
    private JProgressBar progressBar;
    private JLabel lblRecordCount;
    private JLabel lblStatus;
    private Set<String> sentTimestamps;
    
    public TCPClientGUI() {
        setTitle("TCP Client - Batch Energy Transfer (Dataset 1)");
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
        
        btnSend = new JButton("Start TCP Batch Transfer");
        btnSend.setBackground(new Color(50, 150, 200));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFont(new Font("Arial", Font.BOLD, 12));
        topPanel.add(btnSend);
        
        btnClear = new JButton("Clear Log");
        btnClear.setBackground(Color.LIGHT_GRAY);
        topPanel.add(btnClear);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        JTextArea infoArea = new JTextArea();
        infoArea.setText("DATASET 1: IoT Smart Grid\n");
        infoArea.append("Records: 8,737 rows\n");
        infoArea.append("Key field: Power_Consumption_kWh\n");
        infoArea.append("Analysis: Total usage by device, energy source ranking\n");
        infoArea.append("Data Validation: Missing values filtered, duplicate timestamps removed, range check (0-10 kWh)\n");
        infoArea.setEditable(false);
        infoArea.setBackground(new Color(240, 248, 255));
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        centerPanel.add(infoArea, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(50, 150, 200));
        bottomPanel.add(progressBar, BorderLayout.NORTH);
        
        txtStatus = new JTextArea(12, 45);
        txtStatus.setEditable(false);
        txtStatus.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(txtStatus);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Transfer Status"));
        
        JPanel statusBar = new JPanel(new BorderLayout());
        lblRecordCount = new JLabel("Records: 0 / 8737 (duplicates filtered: 0)");
        lblStatus = new JLabel("Ready");
        lblStatus.setForeground(Color.GRAY);
        statusBar.add(lblRecordCount, BorderLayout.WEST);
        statusBar.add(lblStatus, BorderLayout.EAST);
        
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        bottomPanel.add(statusBar, BorderLayout.SOUTH);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        btnSend.addActionListener(e -> startTransfer());
        btnClear.addActionListener(e -> txtStatus.setText(""));
    }
    
    private void startTransfer() {
        btnSend.setEnabled(false);
        sentTimestamps.clear();
        txtStatus.append("=== TCP BATCH TRANSFER STARTED ===\n");
        txtStatus.append("Server: " + txtServerIP.getText() + ":8888\n");
        txtStatus.append("Data Validation: Duplicate filtering ENABLED\n");
        txtStatus.append("------------------------------------\n\n");
        
        lblStatus.setText("Connecting...");
        lblStatus.setForeground(Color.ORANGE);
        progressBar.setIndeterminate(true);
        
        new Thread(() -> {
            try {
                TCPClient client = new TCPClient(txtServerIP.getText());
                
                PrintStream ps = new PrintStream(new TextAreaOutputStream(txtStatus, progressBar, lblRecordCount, lblStatus, sentTimestamps));
                System.setOut(ps);
                
                client.sendBatchData("data/iiot_smart_grid_dataset.csv");
                
                SwingUtilities.invokeLater(() -> {
                    txtStatus.append("\n=== TRANSFER COMPLETE ===\n");
                    lblStatus.setText("Complete");
                    lblStatus.setForeground(Color.GREEN);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    btnSend.setEnabled(true);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    txtStatus.append("\nERROR: " + e.getMessage() + "\n");
                    lblStatus.setText("Error");
                    lblStatus.setForeground(Color.RED);
                    progressBar.setIndeterminate(false);
                    btnSend.setEnabled(true);
                });
            }
        }).start();
    }
    
    static class TextAreaOutputStream extends java.io.OutputStream {
        private JTextArea textArea;
        private JProgressBar progressBar;
        private JLabel recordLabel;
        private JLabel statusLabel;
        private Set<String> sentTimestamps;
        private StringBuilder buffer = new StringBuilder();
        private int duplicateCount = 0;
        
        public TextAreaOutputStream(JTextArea textArea, JProgressBar progressBar, JLabel recordLabel, JLabel statusLabel, Set<String> sentTimestamps) {
            this.textArea = textArea;
            this.progressBar = progressBar;
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
                    
                    if (line.contains("Sent") && line.contains("/")) {
                        try {
                            String sentStr = line.substring(line.indexOf("Sent") + 5, line.indexOf("/"));
                            String totalStr = line.substring(line.indexOf("/") + 1, line.indexOf(" records"));
                            int sent = Integer.parseInt(sentStr.trim());
                            int total = Integer.parseInt(totalStr.trim());
                            int percent = (sent * 100) / total;
                            progressBar.setValue(percent);
                            progressBar.setString(percent + "%");
                            progressBar.setIndeterminate(false);
                            recordLabel.setText("Records: " + sent + " / " + total + " (duplicates filtered: " + duplicateCount + ")");
                            statusLabel.setText("Sending " + percent + "%");
                            statusLabel.setForeground(Color.BLUE);
                        } catch (Exception e) {}
                    }
                    
                    if (line.contains("connected to")) {
                        statusLabel.setText("Connected");
                        statusLabel.setForeground(Color.GREEN);
                    }
                });
                buffer.setLength(0);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TCPClientGUI().setVisible(true));
    }
}