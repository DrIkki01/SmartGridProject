package com.smartgrid.udp;

import com.smartgrid.model.EnergyRecord;
import java.io.*;
import java.net.*;
import java.util.*;

public class UDPServer {
    private static final int PORT = 9876;
    private static final double ENERGY_THRESHOLD = 500.0; // Watts
    private static final double SPIKE_FACTOR = 1.5; // 50% above average = spike
    
    private DatagramSocket socket;
    private boolean running;
    
    // Analysis variables
    private int totalRecordsReceived = 0;
    private int thresholdViolationCount = 0;
    private int unusualSpikeCount = 0;
    private double runningEnergySum = 0.0;
    private double runningAverage = 0.0;
    private double lastEnergyValue = 0.0;
    
    // Store unusual spikes for reporting
    private List<String> spikeLog = new ArrayList<>();
    private List<String> violationLog = new ArrayList<>();
    
    public UDPServer() {
        try {
            socket = new DatagramSocket(PORT);
            running = true;
            System.out.println("UDP Server started on port " + PORT);
            System.out.println("Waiting for live energy data...");
            System.out.println("----------------------------------------");
        } catch (SocketException e) {
            System.err.println("Failed to start UDP Server: " + e.getMessage());
        }
    }
    
    public void start() {
        while (running) {
            try {
                // Receive packet
                byte[] buffer = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                // Check for end of stream
                String received = new String(packet.getData(), 0, packet.getLength());
                if (received.equals("END_OF_STREAM")) {
                    System.out.println("\n--- LIVE STREAM ENDED ---");
                    printFinalReport();
                    running = false;
                    break;
                }
                
                // Parse and process the record
                EnergyRecord record = parseRecord(received);
                if (record != null) {
                    processRecord(record);
                    displayLiveUpdate(record);
                }
                
            } catch (IOException e) {
                System.err.println("Error receiving packet: " + e.getMessage());
            }
        }
        close();
    }
    
    private EnergyRecord parseRecord(String data) {
        try {
            // Format: "deviceId|deviceType|activePower|totalEnergy|timestamp"
            String[] parts = data.split("\\|");
            if (parts.length >= 5) {
                EnergyRecord record = new EnergyRecord();
                record.setDeviceId(parts[0]);
                record.setDeviceType(parts[1]);
                record.setActivePower(Double.parseDouble(parts[2]));
                record.setGridEnergy(Double.parseDouble(parts[3]));
                record.setTimestamp(java.time.LocalDateTime.parse(parts[4]));
                return record;
            }
        } catch (Exception e) {
            System.err.println("Failed to parse record: " + e.getMessage());
        }
        return null;
    }
    
    private void processRecord(EnergyRecord record) {
        totalRecordsReceived++;
        double energyValue = record.getGridEnergy();
        
        // Update running statistics
        runningEnergySum += energyValue;
        runningAverage = runningEnergySum / totalRecordsReceived;
        
        // Analysis 1: Threshold Violation Detection
        if (energyValue > ENERGY_THRESHOLD) {
            thresholdViolationCount++;
            String violation = String.format("[VIOLATION #%d] Device: %s | Energy: %.2f W (Threshold: %.0f W)",
                thresholdViolationCount, record.getDeviceId(), energyValue, ENERGY_THRESHOLD);
            violationLog.add(violation);
            System.out.println("⚠️  " + violation);
        }
        
        // Analysis 2: Unusual Spike Detection
        if (totalRecordsReceived > 1) {
            double expectedMax = runningAverage * SPIKE_FACTOR;
            if (energyValue > expectedMax && energyValue > lastEnergyValue * 1.2) {
                unusualSpikeCount++;
                String spike = String.format("[SPIKE #%d] Device: %s | Energy: %.2f W (Avg: %.2f W, Last: %.2f W)",
                    unusualSpikeCount, record.getDeviceId(), energyValue, runningAverage, lastEnergyValue);
                spikeLog.add(spike);
                System.out.println("⚡🔊 " + spike);
            }
        }
        
        lastEnergyValue = energyValue;
    }
    
    private void displayLiveUpdate(EnergyRecord record) {
        System.out.println("\n--- LIVE UPDATE #" + totalRecordsReceived + " ---");
        System.out.println("Device: " + record.getDeviceId() + " (" + record.getDeviceType() + ")");
        System.out.println("Energy: " + String.format("%.2f", record.getGridEnergy()) + " W");
        System.out.println("Running Avg: " + String.format("%.2f", runningAverage) + " W");
        System.out.println("Threshold Violations: " + thresholdViolationCount);
        System.out.println("Unusual Spikes: " + unusualSpikeCount);
        System.out.println("----------------------------------------");
    }
    
    private void printFinalReport() {
        System.out.println("\n========================================");
        System.out.println("     UDP LIVE ANALYSIS FINAL REPORT");
        System.out.println("========================================");
        System.out.println("Total Records Received: " + totalRecordsReceived);
        System.out.println("Final Running Average: " + String.format("%.2f", runningAverage) + " W");
        System.out.println("Threshold Violations (> " + ENERGY_THRESHOLD + "W): " + thresholdViolationCount);
        System.out.println("Unusual Spikes Detected: " + unusualSpikeCount);
        System.out.println("========================================");
        
        if (!violationLog.isEmpty()) {
            System.out.println("\n--- VIOLATION LOG ---");
            for (String v : violationLog) {
                System.out.println(v);
            }
        }
        
        if (!spikeLog.isEmpty()) {
            System.out.println("\n--- SPIKE LOG ---");
            for (String s : spikeLog) {
                System.out.println(s);
            }
        }
        
        // Save report to file
        saveReportToFile();
    }
    
    private void saveReportToFile() {
        try {
            PrintWriter writer = new PrintWriter("UDP_Analysis_Report.txt");
            writer.println("UDP LIVE ANALYSIS REPORT");
            writer.println("========================");
            writer.println("Total Records Received: " + totalRecordsReceived);
            writer.println("Final Running Average: " + String.format("%.2f", runningAverage) + " W");
            writer.println("Threshold Violations (> " + ENERGY_THRESHOLD + "W): " + thresholdViolationCount);
            writer.println("Unusual Spikes: " + unusualSpikeCount);
            writer.println("\nViolation Log:");
            for (String v : violationLog) {
                writer.println(v);
            }
            writer.println("\nSpike Log:");
            for (String s : spikeLog) {
                writer.println(s);
            }
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