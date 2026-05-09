package com.smartgrid.gui;

import com.smartgrid.tcp.TCPServer;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ServerGUI extends JFrame {
    private JTextArea txtTCPLog;
    private JTextArea txtUDPLog;
    private JButton btnRefreshReports;
    private JButton btnClearLogs;
    private JButton btnStartTCP;
    private JButton btnStartUDP;
    
    private UDPServerHandler udpHandler;
    
    public ServerGUI() {
        setTitle("SmartGrid Server - IoT Energy Monitor");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel - Buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnStartTCP = new JButton("🔌 Start TCP Server");
        btnStartUDP = new JButton("📡 Start UDP Server");
        btnRefreshReports = new JButton("🔄 Refresh Reports");
        btnClearLogs = new JButton("🗑️ Clear All Logs");
        
        btnClearLogs.setBackground(new Color(200, 80, 80));
        btnClearLogs.setForeground(Color.WHITE);
        
        topPanel.add(btnStartTCP);
        topPanel.add(btnStartUDP);
        topPanel.add(btnRefreshReports);
        topPanel.add(btnClearLogs);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Center panel - Split panes
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // TCP Panel
        JPanel tcpPanel = new JPanel(new BorderLayout());
        tcpPanel.setBorder(BorderFactory.createTitledBorder("TCP Batch Analysis"));
        txtTCPLog = new JTextArea(25, 40);
        txtTCPLog.setEditable(false);
        txtTCPLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        tcpPanel.add(new JScrollPane(txtTCPLog), BorderLayout.CENTER);
        
        // UDP Panel
        JPanel udpPanel = new JPanel(new BorderLayout());
        udpPanel.setBorder(BorderFactory.createTitledBorder("UDP Live Analysis"));
        txtUDPLog = new JTextArea(25, 40);
        txtUDPLog.setEditable(false);
        txtUDPLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        udpPanel.add(new JScrollPane(txtUDPLog), BorderLayout.CENTER);
        
        splitPane.setLeftComponent(tcpPanel);
        splitPane.setRightComponent(udpPanel);
        splitPane.setDividerLocation(450);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        add(mainPanel);
        
        // Button actions
        btnStartTCP.addActionListener(e -> startTCPServer());
        btnStartUDP.addActionListener(e -> startUDPServer());
        btnRefreshReports.addActionListener(e -> loadReportsFromFile());
        btnClearLogs.addActionListener(e -> clearAllLogs());
        
        loadReportsFromFile();
    }
    
    private void startTCPServer() {
        btnStartTCP.setEnabled(false);
        appendToTCPLog("Starting TCP Server on port 8888...\n");
        
        new Thread(() -> {
            try {
                TCPServer server = new TCPServer();
                server.start();
                SwingUtilities.invokeLater(() -> {
                    appendToTCPLog("✅ TCP Server finished.\n");
                    loadReportsFromFile();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    appendToTCPLog("❌ TCP Server Error: " + e.getMessage() + "\n");
                });
            } finally {
                SwingUtilities.invokeLater(() -> btnStartTCP.setEnabled(true));
            }
        }).start();
    }
    
    private void startUDPServer() {
        btnStartUDP.setEnabled(false);
        appendToUDPLog("Starting UDP Server on port 9876...\n");
        appendToUDPLog("Waiting for live data...\n");
        
        // Clear previous UDP display before new run
        txtUDPLog.setText("");
        appendToUDPLog("=== NEW UDP SESSION STARTED ===\n");
        appendToUDPLog("Starting UDP Server on port 9876...\n");
        appendToUDPLog("Waiting for live data...\n\n");
        
        new Thread(() -> {
            try {
                udpHandler = new UDPServerHandler(txtUDPLog);
                udpHandler.start();
                SwingUtilities.invokeLater(() -> {
                    appendToUDPLog("\n✅ UDP Server finished.\n");
                    loadReportsFromFile();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    appendToUDPLog("❌ UDP Server Error: " + e.getMessage() + "\n");
                });
            } finally {
                SwingUtilities.invokeLater(() -> btnStartUDP.setEnabled(true));
            }
        }).start();
    }
    
    private void loadReportsFromFile() {
        // Load TCP report
        try {
            File tcpFile = new File("TCP_Analysis_Report.txt");
            if (tcpFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(tcpFile));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();
                txtTCPLog.setText(content.toString());
            } else {
                txtTCPLog.setText("No TCP report found. Run TCP Server first.\n");
            }
        } catch (IOException e) {
            txtTCPLog.setText("Error loading TCP report: " + e.getMessage());
        }
        
        // Load UDP report
        try {
            File udpFile = new File("UDP_Analysis_Report.txt");
            if (udpFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(udpFile));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();
                // Only update if not in live mode
                if (udpHandler == null || !udpHandler.isRunning()) {
                    txtUDPLog.setText(content.toString());
                }
            } else {
                if (udpHandler == null || !udpHandler.isRunning()) {
                    txtUDPLog.setText("No UDP report found. Run UDP Server first.\n");
                }
            }
        } catch (IOException e) {
            txtUDPLog.setText("Error loading UDP report: " + e.getMessage());
        }
    }
    
    private void clearAllLogs() {
        txtTCPLog.setText("");
        txtUDPLog.setText("");
        appendToTCPLog("Logs cleared.\n");
        appendToUDPLog("Logs cleared. Start UDP server for live updates.\n");
    }
    
    private void appendToTCPLog(String message) {
        txtTCPLog.append(message);
        txtTCPLog.setCaretPosition(txtTCPLog.getDocument().getLength());
    }
    
    private void appendToUDPLog(String message) {
        txtUDPLog.append(message);
        txtUDPLog.setCaretPosition(txtUDPLog.getDocument().getLength());
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ServerGUI().setVisible(true);
        });
    }
}