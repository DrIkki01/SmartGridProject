package com.smartgrid.gui;

import com.smartgrid.tcp.TCPServer;
import javax.swing.*;
import java.awt.*;
import java.io.*;

public class ServerGUI extends JFrame {
    private JTextArea txtTCPLog;
    private JTextArea txtUDPLog;
    private JButton btnRefreshReports;
    private JButton btnStartTCP;
    private JButton btnStartUDP;
    
    public ServerGUI() {
        setTitle("SmartGrid Server - IoT Energy Monitor");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel - Buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnStartTCP = new JButton("🔌 Start TCP Server");
        btnStartUDP = new JButton("📡 Start UDP Server");
        btnRefreshReports = new JButton("🔄 Refresh Reports");
        
        topPanel.add(btnStartTCP);
        topPanel.add(btnStartUDP);
        topPanel.add(btnRefreshReports);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Center panel - Split panes for TCP and UDP logs
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // TCP Panel
        JPanel tcpPanel = new JPanel(new BorderLayout());
        tcpPanel.setBorder(BorderFactory.createTitledBorder("TCP Batch Analysis"));
        txtTCPLog = new JTextArea(20, 35);
        txtTCPLog.setEditable(false);
        txtTCPLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        tcpPanel.add(new JScrollPane(txtTCPLog), BorderLayout.CENTER);
        
        // UDP Panel
        JPanel udpPanel = new JPanel(new BorderLayout());
        udpPanel.setBorder(BorderFactory.createTitledBorder("UDP Live Analysis"));
        txtUDPLog = new JTextArea(20, 35);
        txtUDPLog.setEditable(false);
        txtUDPLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        udpPanel.add(new JScrollPane(txtUDPLog), BorderLayout.CENTER);
        
        splitPane.setLeftComponent(tcpPanel);
        splitPane.setRightComponent(udpPanel);
        splitPane.setDividerLocation(400);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        add(mainPanel);
        
        // Button actions
        btnStartTCP.addActionListener(e -> startTCPServer());
        btnStartUDP.addActionListener(e -> startUDPServer());
        btnRefreshReports.addActionListener(e -> loadReports());
        
        // Load existing reports on startup
        loadReports();
    }
    
    private void startTCPServer() {
        btnStartTCP.setEnabled(false);
        txtTCPLog.append("Starting TCP Server on port 8888...\n");
        
        new Thread(() -> {
            try {
                TCPServer server = new TCPServer();
                server.start();
                SwingUtilities.invokeLater(() -> {
                    txtTCPLog.append("✅ TCP Server finished.\n");
                    loadReports();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    txtTCPLog.append("❌ TCP Server Error: " + e.getMessage() + "\n");
                });
            } finally {
                SwingUtilities.invokeLater(() -> btnStartTCP.setEnabled(true));
            }
        }).start();
    }
    
    private void startUDPServer() {
        btnStartUDP.setEnabled(false);
        txtUDPLog.append("Starting UDP Server on port 9876...\n");
        
        new Thread(() -> {
            try {
                com.smartgrid.udp.UDPServer server = new com.smartgrid.udp.UDPServer();
                server.start();
                SwingUtilities.invokeLater(() -> {
                    txtUDPLog.append("✅ UDP Server finished.\n");
                    loadReports();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    txtUDPLog.append("❌ UDP Server Error: " + e.getMessage() + "\n");
                });
            } finally {
                SwingUtilities.invokeLater(() -> btnStartUDP.setEnabled(true));
            }
        }).start();
    }
    
    private void loadReports() {
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
                txtUDPLog.setText(content.toString());
            } else {
                txtUDPLog.setText("No UDP report found. Run UDP Server first.\n");
            }
        } catch (IOException e) {
            txtUDPLog.setText("Error loading UDP report: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ServerGUI().setVisible(true);
        });
    }
}