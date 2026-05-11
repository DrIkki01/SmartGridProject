package com.smartgrid.gui;

import com.smartgrid.udp.UDPServerHandler;
import javax.swing.*;
import java.awt.*;

public class UDPServerGUI extends JFrame {
    private JButton btnStart;
    private JButton btnClear;
    private JButton btnSave;
    private JTextArea txtLog;
    private JLabel lblStatus;
    private UDPServerHandler serverHandler;
    
    public UDPServerGUI() {
        setTitle("UDP Server - Live Energy Monitor (Dataset 2)");
        setSize(900, 720);  // 20% larger (was 750x600)
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Control"));
        
        btnStart = new JButton("Start UDP Server");
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
        
        JLabel infoLabel = new JLabel("  |  Port: 9876  |  Threshold: >4.0 kWh  |  Spike: >2x previous  |  LIVE UPDATES");
        infoLabel.setForeground(new Color(0, 100, 0));
        infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        topPanel.add(infoLabel);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        txtLog = new JTextArea(24, 55);
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(txtLog);
        scrollPane.setBorder(BorderFactory.createTitledBorder("LIVE ANALYSIS - Total_Energy_kWh (Data Validation: Range Check, Type Check, Duplicate Detection)"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblStatus = new JLabel("Server Stopped");
        lblStatus.setForeground(Color.RED);
        lblStatus.setFont(new Font("Arial", Font.BOLD, 11));
        bottomPanel.add(lblStatus);
        
        JLabel hintLabel = new JLabel("  |  Data Validation: Missing values filtered | Invalid format caught | Range check (0-50 kWh) | Duplicate detection | Malformed records rejected");
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        bottomPanel.add(hintLabel);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        btnStart.addActionListener(e -> startServer());
        btnClear.addActionListener(e -> txtLog.setText(""));
        btnSave.addActionListener(e -> saveReport());
    }
    
    private void startServer() {
        btnStart.setEnabled(false);
        txtLog.setText("");
        txtLog.append("========================================\n");
        txtLog.append("  UDP SERVER STARTED - LIVE MODE\n");
        txtLog.append("  Date: " + new java.util.Date() + "\n");
        txtLog.append("  Port: 9876\n");
        txtLog.append("  Analyzing: Total_Energy_kWh\n");
        txtLog.append("  Threshold: > 4.0 kWh\n");
        txtLog.append("  Spike: > 2x previous record\n");
        txtLog.append("  Data Validation: Active (range, type, duplicate, malformed)\n");
        txtLog.append("========================================\n");
        txtLog.append("Waiting for UDP packets...\n");
        txtLog.append("------------------------------------------------------------------------\n\n");
        
        lblStatus.setText("Server Running - Waiting for data");
        lblStatus.setForeground(new Color(255, 140, 0));
        
        new Thread(() -> {
            try {
                serverHandler = new UDPServerHandler(txtLog);
                serverHandler.start();
                SwingUtilities.invokeLater(() -> {
                    txtLog.append("\n========================================\n");
                    txtLog.append("  UDP SERVER FINISHED\n");
                    txtLog.append("========================================\n");
                    lblStatus.setText("Server Stopped");
                    lblStatus.setForeground(Color.RED);
                    btnStart.setEnabled(true);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    txtLog.append("\nERROR: " + e.getMessage() + "\n");
                    lblStatus.setText("Error: " + e.getMessage());
                    lblStatus.setForeground(Color.RED);
                    btnStart.setEnabled(true);
                });
            }
        }).start();
    }
    
    private void saveReport() {
        try {
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String filename = "UDP_Live_Report_" + timestamp + ".txt";
            java.io.PrintWriter writer = new java.io.PrintWriter(filename);
            writer.println("UDP LIVE ANALYSIS REPORT");
            writer.println("Generated: " + new java.util.Date());
            writer.println("========================================");
            writer.println(txtLog.getText());
            writer.close();
            JOptionPane.showMessageDialog(this, "Report saved to: " + filename);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UDPServerGUI().setVisible(true));
    }
}