package com.smartgrid.gui;

import com.smartgrid.tcp.TCPClient;
import com.smartgrid.udp.UDPClient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUI extends JFrame {
    private JButton btnTCP;
    private JButton btnUDP;
    private JTextArea txtStatus;
    private JProgressBar progressBar;
    private JTextField txtServerIP;
    private JSpinner spinDelay;
    
    public ClientGUI() {
        setTitle("SmartGrid Client - IoT Energy Monitor");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel - Server IP
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Server IP:"));
        txtServerIP = new JTextField("localhost", 15);
        topPanel.add(txtServerIP);
        topPanel.add(new JLabel("Delay (ms):"));
        spinDelay = new JSpinner(new SpinnerNumberModel(1000, 100, 5000, 100));
        topPanel.add(spinDelay);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Center panel - Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        
        btnTCP = new JButton("🚀 Start TCP Batch Transfer");
        btnTCP.setFont(new Font("Arial", Font.BOLD, 14));
        btnTCP.setBackground(new Color(50, 150, 200));
        btnTCP.setForeground(Color.WHITE);
        
        btnUDP = new JButton("📡 Start UDP Live Stream");
        btnUDP.setFont(new Font("Arial", Font.BOLD, 14));
        btnUDP.setBackground(new Color(50, 200, 100));
        btnUDP.setForeground(Color.WHITE);
        
        buttonPanel.add(btnTCP);
        buttonPanel.add(btnUDP);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // Bottom panel - Status
        txtStatus = new JTextArea(10, 40);
        txtStatus.setEditable(false);
        txtStatus.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(txtStatus);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Status"));
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(progressBar, BorderLayout.NORTH);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Button actions
        btnTCP.addActionListener(e -> startTCP());
        btnUDP.addActionListener(e -> startUDP());
    }
    
    private void startTCP() {
        btnTCP.setEnabled(false);
        txtStatus.append("Starting TCP Batch Transfer...\n");
        progressBar.setIndeterminate(true);
        
        // Run in background thread
        new Thread(() -> {
            try {
                // Set server IP in TCPClient (modify TCPClient to accept IP parameter)
                TCPClient client = new TCPClient(txtServerIP.getText());
                client.sendBatchData("data/iiot_smart_grid_dataset.csv");
                SwingUtilities.invokeLater(() -> {
                    txtStatus.append("✅ TCP Batch Transfer Complete!\n");
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    txtStatus.append("❌ TCP Error: " + e.getMessage() + "\n");
                    progressBar.setIndeterminate(false);
                });
            } finally {
                SwingUtilities.invokeLater(() -> btnTCP.setEnabled(true));
            }
        }).start();
    }
    
    private void startUDP() {
        btnUDP.setEnabled(false);
        txtStatus.append("Starting UDP Live Stream...\n");
        progressBar.setIndeterminate(true);
        
        new Thread(() -> {
            try {
                // Get delay from spinner
                int delay = (Integer) spinDelay.getValue();
                UDPClient client = new UDPClient(txtServerIP.getText(), delay);
                client.sendLiveFeed("data/iiot_smart_grid_dataset.csv");
                SwingUtilities.invokeLater(() -> {
                    txtStatus.append("✅ UDP Live Stream Complete!\n");
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    txtStatus.append("❌ UDP Error: " + e.getMessage() + "\n");
                    progressBar.setIndeterminate(false);
                });
            } finally {
                SwingUtilities.invokeLater(() -> btnUDP.setEnabled(true));
            }
        }).start();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientGUI().setVisible(true);
        });
    }
}