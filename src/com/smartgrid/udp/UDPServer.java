package com.smartgrid.udp;

import java.io.*;
import java.net.*;

public class UDPServer {
    private static final int PORT = 9876;
    private static final double ENERGY_THRESHOLD = 4.0; // kWh threshold
    
    private DatagramSocket socket;
    private boolean running;
    
    // Analysis counters
    private int totalRecords = 0;
    private int thresholdViolations = 0;
    private int spikeCount = 0;
    
    private double runningSum = 0.0;
    private double runningAverage = 0.0;
    private double lastPower = 0.0;
    private double highestPower = 0.0;
    private String highestTimestamp = "";
    
    private StringBuilder spikeLog = new StringBuilder();
    private StringBuilder violationLog = new StringBuilder();
    
    public UDPServer() {
        try {
            socket = new DatagramSocket(PORT);
            running = true;
            System.out.println("UDP Server started on port " + PORT);
        } catch (SocketException e) {
            System.err.println("Failed to start: " + e.getMessage());
        }
    }
    
    public void start() {
        while (running) {
            try {
                byte[] buffer = new byte[65535];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String received = new String(packet.getData(), 0, packet.getLength());
                
                if (received.equals("END_OF_STREAM")) {
                    printFinalReport();
                    running = false;
                    break;
                }
                
                processRecord(received);
                
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        close();
    }
    
    private void processRecord(String data) {
        totalRecords++;
        
        // Parse: timestamp|power|temp|humidity|weather|efficiency
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
            
            // Track highest power so far
            if (power > highestPower) {
                highestPower = power;
                highestTimestamp = timestamp;
                System.out.println("🏆 NEW HIGHEST POWER: " + String.format("%.2f", power) + " kWh at " + timestamp);
            }
            
            // Analysis 2: Threshold Violation Detection
            if (power > ENERGY_THRESHOLD) {
                thresholdViolations++;
                String violation = String.format("[VIOLATION #%d] %.2f kWh at %s (Temp: %.1f°C, Humidity: %.1f%%)",
                    thresholdViolations, power, timestamp, temperature, humidity);
                violationLog.append(violation).append("\n");
                System.out.println("⚠️ " + violation);
            }
            
            // Analysis 1: Unusual Spike Detection (jump > 2x from last value)
            if (totalRecords > 1 && power > lastPower * 2.0) {
                spikeCount++;
                String spike = String.format("[SPIKE #%d] %.2f kWh (from %.2f kWh) at %s",
                    spikeCount, power, lastPower, timestamp);
                spikeLog.append(spike).append("\n");
                System.out.println("⚡🔊 " + spike);
            }
            
            lastPower = power;
            
            // Live display update
            displayLiveUpdate(timestamp, power, temperature, humidity, weather, efficiency);
        }
    }
    
    private void displayLiveUpdate(String timestamp, double power, double temp, double humidity, String weather, double efficiency) {
        System.out.println("\n--- LIVE UPDATE #" + totalRecords + " ---");
        System.out.println("Timestamp: " + timestamp);
        System.out.println("Power: " + String.format("%.2f", power) + " kWh");
        System.out.println("Temperature: " + String.format("%.1f", temp) + "°C");
        System.out.println("Humidity: " + String.format("%.1f", humidity) + "%");
        System.out.println("Weather: " + weather);
        System.out.println("Efficiency Score: " + String.format("%.0f", efficiency));
        System.out.println("Running Avg Power: " + String.format("%.2f", runningAverage) + " kWh");
        System.out.println("Threshold Violations: " + thresholdViolations);
        System.out.println("Unusual Spikes: " + spikeCount);
        System.out.println("----------------------------------------");
    }
    
    private void printFinalReport() {
        System.out.println("\n========================================");
        System.out.println("     UDP LIVE ANALYSIS FINAL REPORT");
        System.out.println("========================================");
        System.out.println("Total Records Received: " + totalRecords);
        System.out.println("Final Running Average: " + String.format("%.2f", runningAverage) + " kWh");
        System.out.println("Highest Power Recorded: " + String.format("%.2f", highestPower) + " kWh at " + highestTimestamp);
        System.out.println("Threshold Violations (> " + ENERGY_THRESHOLD + " kWh): " + thresholdViolations);
        System.out.println("Unusual Spikes Detected: " + spikeCount);
        System.out.println("========================================");
        
        if (violationLog.length() > 0) {
            System.out.println("\n--- VIOLATION LOG ---");
            System.out.print(violationLog.toString());
        }
        
        if (spikeLog.length() > 0) {
            System.out.println("\n--- SPIKE LOG ---");
            System.out.print(spikeLog.toString());
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
            writer.println("Highest Power Recorded: " + String.format("%.2f", highestPower) + " kWh");
            writer.println("Threshold Violations (> " + ENERGY_THRESHOLD + " kWh): " + thresholdViolations);
            writer.println("Unusual Spikes Detected: " + spikeCount);
            writer.println("\nViolation Log:");
            writer.print(violationLog.toString());
            writer.println("\nSpike Log:");
            writer.print(spikeLog.toString());
            writer.close();
            System.out.println("\n✅ Report saved to: UDP_Analysis_Report.txt");
        } catch (FileNotFoundException e) {
            System.err.println("Failed to save report: " + e.getMessage());
        }
    }
    
    private void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            System.out.println("\nUDP Server stopped.");
        }
    }
    
    public static void main(String[] args) {
        UDPServer server = new UDPServer();
        server.start();
    }
}