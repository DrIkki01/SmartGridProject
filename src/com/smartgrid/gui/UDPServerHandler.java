package com.smartgrid.gui;

import javax.swing.*;
import java.io.*;
import java.net.*;

public class UDPServerHandler {
    private static final int PORT = 9876;
    private static final double ENERGY_THRESHOLD = 4.0;
    
    private DatagramSocket socket;
    private boolean running;
    private JTextArea logArea;
    
    // Analysis counters
    private int totalRecords = 0;
    private int thresholdViolations = 0;
    private int spikeCount = 0;
    private double runningSum = 0.0;
    private double runningAverage = 0.0;
    private double lastPower = 0.0;
    private double highestPower = 0.0;
    private String highestTimestamp = "";
    
    private StringBuilder violationLog = new StringBuilder();
    private StringBuilder spikeLog = new StringBuilder();
    
        public UDPServerHandler(JTextArea logArea) {
            this.logArea = logArea;
            try {
                socket = new DatagramSocket(PORT);
                running = true;
            } catch (SocketException e) {
                appendLog("Failed to start UDP Server: " + e.getMessage());
            }
        }
    
        public void start() {
            appendLog("UDP Server listening on port " + PORT);
            appendLog("Threshold: > " + ENERGY_THRESHOLD + " kWh");
            appendLog("----------------------------------------\n");

            while (running) {
                try {
                byte[] buffer = new byte[65535];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Get sender info
                InetAddress senderIP = packet.getAddress();
                int senderPort = packet.getPort();

                String received = new String(packet.getData(), 0, packet.getLength());

                if (received.equals("END_OF_STREAM")) {
                    printFinalReport();
                    running = false;
                    break;
                }

                // Pass sender info to processRecord
                processRecord(received, senderIP, senderPort);

                } catch (IOException e) {
                    appendLog("Error: " + e.getMessage());
                }
            }
        close();
        }
    
        public boolean isRunning() {
            return running;
        }
    
        private void processRecord(String data, InetAddress senderIP, int senderPort) {
        totalRecords++;
    
        String senderInfo = senderIP.getHostAddress() + ":" + senderPort;
    
        // Show who sent the data in GUI
        appendLog("📡 Data received from: " + senderInfo);
    
        String[] parts = data.split("\\|");
        if (parts.length >= 6) {
            String timestamp = parts[0];
            double power = Double.parseDouble(parts[1]);
            double temperature = Double.parseDouble(parts[2]);
            double humidity = Double.parseDouble(parts[3]);
            String weather = parts[4];
            double efficiency = Double.parseDouble(parts[5]);
        
            // Update running statistics
            runningSum += power;
            runningAverage = runningSum / totalRecords;
        
            // Track highest power with sender info
            if (power > highestPower) {
                highestPower = power;
                highestTimestamp = timestamp;
                appendLog("🏆 NEW HIGHEST POWER: " + String.format("%.2f", power) + " kWh from " + senderInfo + " at " + timestamp);
            }
        
            // Threshold violation with sender IP
            if (power > ENERGY_THRESHOLD) {
                thresholdViolations++;
                String violation = String.format("⚠️ VIOLATION #%d: %.2f kWh from %s at %s", 
                    thresholdViolations, power, senderInfo, timestamp);
                violationLog.append(violation).append("\n");
                appendLog(violation);
            }
        
            // Spike detection with sender IP
            if (totalRecords > 1 && power > lastPower * 2.0) {
                spikeCount++;
                String spike = String.format("⚡ SPIKE #%d: %.2f kWh (from %.2f) from %s at %s",
                    spikeCount, power, lastPower, senderInfo, timestamp);
                spikeLog.append(spike).append("\n");
                appendLog(spike);
            }
        
            lastPower = power;
        
            // Live update display in GUI
            appendLog(String.format("\n--- LIVE UPDATE #%d ---", totalRecords));
            appendLog(String.format("Time: %s | Power: %.2f kWh", timestamp, power));
            appendLog(String.format("Sender: %s", senderInfo));
            appendLog(String.format("Temp: %.1f°C | Humidity: %.1f%% | Weather: %s", temperature, humidity, weather));
            appendLog(String.format("Running Avg: %.2f kWh | Violations: %d | Spikes: %d", 
                runningAverage, thresholdViolations, spikeCount));
            appendLog("----------------------------------------");
        }
    }
    
    private void printFinalReport() {
        appendLog("\n========================================");
        appendLog("     UDP LIVE ANALYSIS FINAL REPORT");
        appendLog("========================================");
        appendLog("Total Records Received: " + totalRecords);
        appendLog("Final Running Average: " + String.format("%.2f", runningAverage) + " kWh");
        appendLog("Highest Power: " + String.format("%.2f", highestPower) + " kWh at " + highestTimestamp);
        appendLog("Threshold Violations (> " + ENERGY_THRESHOLD + " kWh): " + thresholdViolations);
        appendLog("Unusual Spikes Detected: " + spikeCount);
        appendLog("========================================");
        
        if (violationLog.length() > 0) {
            appendLog("\n--- VIOLATION LOG ---");
            appendLog(violationLog.toString());
        }
        
        if (spikeLog.length() > 0) {
            appendLog("\n--- SPIKE LOG ---");
            appendLog(spikeLog.toString());
        }
        
        saveReportToFile();
    }
    
    private void saveReportToFile() {
        try {
            PrintWriter writer = new PrintWriter("UDP_Analysis_Report.txt");
            writer.println("========================================");
            writer.println("     UDP LIVE ANALYSIS FINAL REPORT");
            writer.println("========================================");
            writer.println("Total Records Received: " + totalRecords);
            writer.println("Final Running Average: " + String.format("%.2f", runningAverage) + " kWh");
            writer.println("Highest Power: " + String.format("%.2f", highestPower) + " kWh");
            writer.println("Threshold Violations: " + thresholdViolations);
            writer.println("Unusual Spikes: " + spikeCount);
            writer.println("\nViolation Log:");
            writer.print(violationLog.toString());
            writer.println("\nSpike Log:");
            writer.print(spikeLog.toString());
            writer.close();
            appendLog("\n✅ Report saved to: UDP_Analysis_Report.txt");
        } catch (FileNotFoundException e) {
            appendLog("Failed to save report: " + e.getMessage());
        }
    }
    
    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    private void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            appendLog("UDP Server stopped.");
        }
    }
}