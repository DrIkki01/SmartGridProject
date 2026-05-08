package com.smartgrid.udp;

import java.io.*;
import java.net.*;

public class UDPClient {
    private static final int SERVER_PORT = 9876;
    
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private String serverIP;
    private int delayMs;
    
    // Constructor with IP and delay parameters (for GUI)
    public UDPClient(String serverIP, int delayMs) {
        this.serverIP = serverIP;
        this.delayMs = delayMs;
        try {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName(serverIP);
            System.out.println("UDP Client started. Sending to " + serverIP + ":" + SERVER_PORT);
            System.out.println("Delay between records: " + delayMs + "ms");
        } catch (Exception e) {
            System.err.println("Failed to start UDP Client: " + e.getMessage());
        }
    }
    
    // Default constructor for backward compatibility
    public UDPClient() {
        this("localhost", 1000);
    }
    
    public void sendLiveFeed(String csvFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            int recordCount = 0;
            int maxRecords = 50; // Limit for testing, remove or increase for full dataset
            
            // Read and skip header
            String header = br.readLine();
            System.out.println("CSV Header: " + header);
            System.out.println("Starting live feed simulation...");
            System.out.println("Sending records every " + delayMs + "ms");
            System.out.println("----------------------------------------");
            
            while ((line = br.readLine()) != null && recordCount < maxRecords) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(",");
                if (parts.length >= 20) {
                    String timestamp = parts[0];
                    String powerConsumption = parts[1];
                    String temperature = parts[9];
                    String humidity = parts[10];
                    String weather = parts[11];
                    String efficiency = parts[19];
                    
                    String message = String.format("%s|%s|%s|%s|%s|%s",
                        timestamp, powerConsumption, temperature, humidity, weather, efficiency);
                    
                    sendMessage(message);
                    recordCount++;
                    
                    System.out.println("[" + recordCount + "] " + timestamp + " | Power: " + powerConsumption + " kWh");
                    
                    Thread.sleep(delayMs);
                }
            }
            
            sendMessage("END_OF_STREAM");
            System.out.println("\n----------------------------------------");
            System.out.println("Live feed complete! " + recordCount + " records sent.");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
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
    
    public static void main(String[] args) {
        UDPClient client = new UDPClient();
        client.sendLiveFeed("data/iiot_smart_grid_dataset.csv");
    }
}