package com.smartgrid.gui;

import com.smartgrid.tcp.TCPServer;
import javax.swing.*;
import java.awt.*;
import java.io.*;

public class TCPServerGUI extends JFrame {
    private JButton btnStart;
    private JButton btnClear;
    private JButton btnSave;
    private JTextArea txtLog;
    private JProgressBar progressBar;
    private JLabel lblStatus;
    
    public TCPServerGUI() {
        setTitle("TCP Server - Batch Energy Analyzer (Dataset 1)");
        setSize(840, 660);  // 20% larger (was 700x550)
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Control"));
        
        btnStart = new JButton("Start TCP Server");
        btnStart.setBackground(new Color(50, 200, 50));
        btnStart.setForeground(Color.WHITE);
        btnStart.setFont(new Font("Arial", Font.BOLD, 12));
        topPanel.add(btnStart);
        
        btnClear = new JButton("Clear Log");
        btnClear.setBackground(Color.LIGHT_GRAY);
        topPanel.add(btnClear);
        
        btnSave = new JButton("Save Report");
        btnSave.setBackground(new Color(100, 150, 200));
        btnSave.setForeground(Color.WHITE);
        topPanel.add(btnSave);
        
        JLabel infoLabel = new JLabel("  |  Port: 8888  |  Dataset 1: IoT Smart Grid (8,737 records)  |  Data Validation: Range Check (0-10 kWh), Duplicate Detection");
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        topPanel.add(infoLabel);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        txtLog = new JTextArea(24, 55);
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(txtLog);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Analysis Report (Data Validation Results Included)"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(50, 150, 200));
        bottomPanel.add(progressBar, BorderLayout.NORTH);
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblStatus = new JLabel("Server Stopped");
        lblStatus.setForeground(Color.RED);
        lblStatus.setFont(new Font("Arial", Font.BOLD, 11));
        statusPanel.add(lblStatus);
        
        JLabel validationLabel = new JLabel("  |  Validation: Missing values filtered | Invalid format caught | Type conversion applied | Duplicates removed | Malformed records rejected");
        validationLabel.setForeground(Color.GRAY);
        validationLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        statusPanel.add(validationLabel);
        
        bottomPanel.add(statusPanel, BorderLayout.CENTER);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        btnStart.addActionListener(e -> startServer());
        btnClear.addActionListener(e -> txtLog.setText(""));
        btnSave.addActionListener(e -> saveReport());
    }
    
    private void startServer() {
        btnStart.setEnabled(false);
        txtLog.setText("");
        txtLog.append("=== TCP SERVER STARTED ===\n");
        txtLog.append("Port: 8888\n");
        txtLog.append("Data Validation: Active (range, type, duplicate, malformed)\n");
        txtLog.append("Waiting for client connection...\n");
        txtLog.append("----------------------------------------\n\n");
        
        lblStatus.setText("Server Running - Waiting");
        lblStatus.setForeground(new Color(255, 140, 0));
        progressBar.setIndeterminate(true);
        
        new Thread(() -> {
            try {
                TCPServer server = new TCPServer();
                
                PrintStream ps = new PrintStream(new TextAreaOutputStream(txtLog, progressBar, lblStatus));
                System.setOut(ps);
                
                server.start();
                
                SwingUtilities.invokeLater(() -> {
                    txtLog.append("\n=== SERVER FINISHED ===\n");
                    lblStatus.setText("Complete");
                    lblStatus.setForeground(Color.GREEN);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    btnStart.setEnabled(true);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    txtLog.append("\nERROR: " + e.getMessage() + "\n");
                    lblStatus.setText("Error");
                    lblStatus.setForeground(Color.RED);
                    progressBar.setIndeterminate(false);
                    btnStart.setEnabled(true);
                });
            }
        }).start();
    }
    
    private void saveReport() {
        try {
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String filename = "TCP_Report_" + timestamp + ".txt";
            PrintWriter writer = new PrintWriter(filename);
            writer.println("TCP BATCH ANALYSIS REPORT");
            writer.println("Generated: " + new java.util.Date());
            writer.println("========================================");
            writer.println(txtLog.getText());
            writer.close();
            JOptionPane.showMessageDialog(this, "Report saved to: " + filename);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving: " + e.getMessage());
        }
    }
    
    static class TextAreaOutputStream extends java.io.OutputStream {
        private JTextArea textArea;
        private JProgressBar progressBar;
        private JLabel statusLabel;
        private StringBuilder buffer = new StringBuilder();
        
        public TextAreaOutputStream(JTextArea textArea, JProgressBar progressBar, JLabel statusLabel) {
            this.textArea = textArea;
            this.progressBar = progressBar;
            this.statusLabel = statusLabel;
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
                    
                    if (line.contains("Received") && line.contains("/")) {
                        try {
                            String receivedStr = line.substring(line.indexOf("Received") + 9, line.indexOf("/"));
                            String totalStr = line.substring(line.indexOf("/") + 1, line.indexOf(" records"));
                            int received = Integer.parseInt(receivedStr.trim());
                            int total = Integer.parseInt(totalStr.trim());
                            int percent = (received * 100) / total;
                            progressBar.setValue(percent);
                            progressBar.setString(percent + "%");
                            progressBar.setIndeterminate(false);
                            statusLabel.setText("Receiving " + percent + "%");
                            statusLabel.setForeground(Color.GREEN);
                        } catch (Exception e) {}
                    }
                    
                    if (line.contains("Client connected")) {
                        statusLabel.setText("Client Connected");
                        statusLabel.setForeground(Color.GREEN);
                    }
                    
                    if (line.contains("Report saved")) {
                        statusLabel.setText("Analysis Complete");
                        statusLabel.setForeground(new Color(0, 100, 0));
                    }
                });
                buffer.setLength(0);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TCPServerGUI().setVisible(true));
    }
}