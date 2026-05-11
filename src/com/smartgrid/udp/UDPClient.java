package com.smartgrid.udp;

import java.io.*;
import java.net.*;
import java.util.*;

public class UDPClient {
    private static final int SERVER_PORT = 9876;
    private static final int MAX_RECORDS = 50;  // Limit to 50 records for demo
    
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private String serverIP;
    private int delayMs;
    
    // Validation counters
    private int totalRowsRead = 0;
    private int validRecords = 0;
    private int duplicateRecords = 0;
    private int malformedRecords = 0;
    private int outOfRangeRecords = 0;
    private Set<String> seenTimestamps;
    
    public UDPClient(String serverIP, int delayMs) {
        this.serverIP = serverIP;
        this.delayMs = delayMs;
        this.seenTimestamps = new HashSet<>();
        try {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName(serverIP);
            System.out.println("UDP Client started. Sending to " + serverIP + ":" + SERVER_PORT);
            System.out.println("Delay between records: " + delayMs + "ms");
            System.out.println("Max records to send: " + MAX_RECORDS);
        } catch (Exception e) {
            System.err.println("Failed to start UDP Client: " + e.getMessage());
        }
    }
    
    public void sendLiveFeed(String csvFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            int recordCount = 0;
            
            String header = br.readLine();
            System.out.println("CSV Header: " + header);
            System.out.println("Starting data validation and live feed simulation...");
            System.out.println("----------------------------------------");
            
            // Stop after MAX_RECORDS
            while ((line = br.readLine()) != null && recordCount < MAX_RECORDS) {
                totalRowsRead++;
                
                if (line.trim().isEmpty()) {
                    System.out.println("[VALIDATION] Line " + totalRowsRead + ": Empty line - SKIPPED");
                    malformedRecords++;
                    continue;
                }
                
                String[] parts = line.split(",");
                
                // Check for malformed (need at least 10 columns for our data)
                if (parts.length < 10) {
                    System.out.println("[VALIDATION] Line " + totalRowsRead + ": Malformed - only " + parts.length + " columns - SKIPPED");
                    malformedRecords++;
                    continue;
                }
                
                try {
                    String homeId = parts[0];
                    String timestamp = parts[1];
                    String totalEnergy = parts[8];
                    String temperature = parts[2];
                    String humidity = parts[3];
                    String motion = parts[9];
                    
                    // Check for duplicate timestamp
                    if (seenTimestamps.contains(timestamp)) {
                        System.out.println("[VALIDATION] Line " + totalRowsRead + ": Duplicate timestamp " + timestamp + " - SKIPPED");
                        duplicateRecords++;
                        continue;
                    }
                    
                    double energyValue = Double.parseDouble(totalEnergy);
                    
                    // Range checking (0-50 kWh is normal range for this dataset)
                    if (energyValue < 0 || energyValue > 50) {
                        System.out.println("[VALIDATION] Line " + totalRowsRead + ": Energy out of range (0-50 kWh): " + energyValue + " kWh - SKIPPED");
                        outOfRangeRecords++;
                        continue;
                    }
                    
                    // If all validation passes, send the record
                    String defaultEfficiency = "50";
                    String message = String.format("%s|%s|%s|%s|%s|%s",
                        timestamp, totalEnergy, temperature, humidity, motion, defaultEfficiency);
                    
                    sendMessage(message);
                    validRecords++;
                    seenTimestamps.add(timestamp);
                    recordCount++;
                    
                    System.out.println("[VALIDATION] [" + recordCount + "/" + MAX_RECORDS + "] " + timestamp + " | Home: " + homeId + 
                                     " | Energy: " + totalEnergy + " kWh - VALID");
                    
                    Thread.sleep(delayMs);
                    
                } catch (NumberFormatException e) {
                    System.out.println("[VALIDATION] Line " + totalRowsRead + ": Invalid number format - " + e.getMessage() + " - SKIPPED");
                    malformedRecords++;
                } catch (Exception e) {
                    System.out.println("[VALIDATION] Line " + totalRowsRead + ": Parse error - " + e.getMessage() + " - SKIPPED");
                    malformedRecords++;
                }
            }
            
            sendMessage("END_OF_STREAM");
            
            // Print validation summary
            System.out.println("\n========== DATA VALIDATION SUMMARY ==========");
            System.out.println("Total rows read: " + totalRowsRead);
            System.out.println("Valid records sent: " + validRecords + " (max: " + MAX_RECORDS + ")");
            System.out.println("Duplicate records (skipped): " + duplicateRecords);
            System.out.println("Malformed records (skipped): " + malformedRecords);
            System.out.println("Out of range records (skipped): " + outOfRangeRecords);
            System.out.println("=============================================\n");
            
            System.out.println("Live feed complete! " + validRecords + " records sent.");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            close();
        }
    }
    
    private void sendMessage(String message) {
        try {
            byte[] sendData = message.getBytes();
            DatagramPacket packet = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Failed to send: " + e.getMessage());
        }
    }
    
    private void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            System.out.println("UDP Client closed.");
        }
    }
    
    // Method to change max records (optional)
    public void setMaxRecords(int max) {
        // This would require rework, but for now just change MAX_RECORDS constant
    }
    
    public static void main(String[] args) {
        UDPClient client = new UDPClient("localhost", 1000);
        client.sendLiveFeed("data/smart_home_energy_usage_dataset.csv");
    }
}