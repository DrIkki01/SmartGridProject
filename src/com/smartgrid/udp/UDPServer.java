package com.smartgrid.udp;

import java.io.*;
import java.net.*;

public class UDPServer {
    private static final int PORT = 9876;
    private static final double ENERGY_THRESHOLD = 4.0;
    
    private DatagramSocket socket;
    private boolean running;
    
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
        
                InetAddress senderIP = packet.getAddress();
                int senderPort = packet.getPort();
        
                String received = new String(packet.getData(), 0, packet.getLength());
        
                if (received.equals("END_OF_STREAM")) {
                    printFinalReport();
                    running = false;
                    break;
                }
        
                processRecord(received, senderIP, senderPort);
        
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        close();
    }
    
    private void processRecord(String data, InetAddress senderIP, int senderPort) {
        totalRecords++;
    
        System.out.println("📡 Data received from: " + senderIP.getHostAddress() + ":" + senderPort);
    
        String[] parts = data.split("\\|");
        if (parts.length >= 6) {
            String timestamp = parts[0];
            double power = Double.parseDouble(parts[1]);
            double temperature = Double.parseDouble(parts[2]);
            double humidity = Double.parseDouble(parts[3]);
            String weather = parts[4];
            double efficiency = Double.parseDouble(parts[5]);
        
            runningSum += power;
            runningAverage = runningSum / totalRecords;
        
            if (power > highestPower) {
                highestPower = power;
                highestTimestamp = timestamp;
                System.out.println("🏆 NEW HIGHEST POWER: " + String.format("%.2f", power) + " kWh at " + timestamp);
            }
        
            if (power > ENERGY_THRESHOLD) {
                thresholdViolations++;
                String violation = String.format("⚠️ VIOLATION #%d: %.2f kWh from %s:%d at %s", 
                    thresholdViolations, power, senderIP.getHostAddress(), senderPort, timestamp);
                violationLog.append(violation).append("\n");
                System.out.println(violation);
            }
        
            if (totalRecords > 1 && power > lastPower * 2.0) {
                spikeCount++;
                String spike = String.format("⚡ SPIKE #%d: %.2f kWh (from %.2f) from %s:%d at %s",
                    spikeCount, power, lastPower, senderIP.getHostAddress(), senderPort, timestamp);
                spikeLog.append(spike).append("\n");
                System.out.println(spike);
            }
        
            lastPower = power;
        
            System.out.println("\n--- LIVE UPDATE #" + totalRecords + " ---");
            System.out.println("Time: " + timestamp + " | Power: " + String.format("%.2f", power) + " kWh");
            System.out.println("Sender: " + senderIP.getHostAddress() + ":" + senderPort);
            System.out.println("Temp: " + String.format("%.1f", temperature) + "°C | Humidity: " + String.format("%.1f", humidity) + "% | Weather: " + weather);
            System.out.println("Running Avg: " + String.format("%.2f", runningAverage) + " kWh | Violations: " + thresholdViolations + " | Spikes: " + spikeCount);
            System.out.println("----------------------------------------");
        }
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