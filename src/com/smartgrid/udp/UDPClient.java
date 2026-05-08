package com.smartgrid.udp;

import com.smartgrid.model.EnergyRecord;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UDPClient {
    private static final String SERVER_IP = "localhost"; // Change to actual server IP
    private static final int SERVER_PORT = 9876;
    private static final int DELAY_MS = 1000; // 1 second between records
    
    private DatagramSocket socket;
    private InetAddress serverAddress;
    
    public UDPClient() {
        try {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName(SERVER_IP);
            System.out.println("UDP Client started. Sending to " + SERVER_IP + ":" + SERVER_PORT);
        } catch (Exception e) {
            System.err.println("Failed to start UDP Client: " + e.getMessage());
        }
    }
    
    public void sendLiveFeed(String csvFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            int recordCount = 0;
            
            // Skip header line if exists
            br.readLine();
            
            System.out.println("Starting live feed simulation...");
            System.out.println("Sending records every " + DELAY_MS + "ms");
            System.out.println("----------------------------------------");
            
            while ((line = br.readLine()) != null) {
                EnergyRecord record = parseCSVLine(line);
                if (record != null) {
                    String message = formatForUDP(record);
                    sendMessage(message);
                    recordCount++;
                    
                    System.out.println("[" + recordCount + "] Sent: " + record.getDeviceId() + 
                                     " | Energy: " + String.format("%.2f", record.getGridEnergy()) + " W");
                    
                    // Simulate live feed delay
                    Thread.sleep(DELAY_MS);
                }
            }
            
            // Send end of stream signal
            sendMessage("END_OF_STREAM");
            System.out.println("\n----------------------------------------");
            System.out.println("Live feed complete! " + recordCount + " records sent.");
            System.out.println("END_OF_STREAM signal sent.");
            
        } catch (FileNotFoundException e) {
            System.err.println("CSV file not found: " + csvFilePath);
        } catch (IOException e) {
            System.err.println("Error reading CSV: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Stream interrupted: " + e.getMessage());
        } finally {
            close();
        }
    }
    
    private EnergyRecord parseCSVLine(String line) {
        try {
            // Expected CSV format based on IoT-Enabled Smart Grid Dataset
            // Format: timestamp, hour, dayOfWeek, month, activePower, reactivePower, 
            // voltage, current, gridFrequency, powerFactor, gridEnergy, solarEnergy,
            // windEnergy, hybridEnergy, temperature, humidity, weatherCondition, efficiency
            
            String[] parts = line.split(",");
            if (parts.length >= 18) {
                EnergyRecord record = new EnergyRecord();
                
                // Parse timestamp
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                record.setTimestamp(LocalDateTime.parse(parts[0], formatter));
                
                // Parse numeric values
                record.setHour(Integer.parseInt(parts[1]));
                record.setDayOfWeek(Integer.parseInt(parts[2]));
                record.setMonth(parts[3]);
                record.setActivePower(Double.parseDouble(parts[4]));
                record.setReactivePower(Double.parseDouble(parts[5]));
                record.setVoltage(Double.parseDouble(parts[6]));
                record.setCurrent(Double.parseDouble(parts[7]));
                record.setGridFrequency(Double.parseDouble(parts[8]));
                record.setPowerFactor(Double.parseDouble(parts[9]));
                record.setGridEnergy(Double.parseDouble(parts[10]));
                record.setSolarEnergy(Double.parseDouble(parts[11]));
                record.setWindEnergy(Double.parseDouble(parts[12]));
                record.setHybridEnergy(Double.parseDouble(parts[13]));
                record.setTemperature(Double.parseDouble(parts[14]));
                record.setHumidity(Double.parseDouble(parts[15]));
                record.setWeatherCondition(parts[16]);
                record.setEnergyEfficiencyScore(Integer.parseInt(parts[17]));
                
                // Assign device info (for analysis purposes)
                assignDeviceInfo(record);
                
                return record;
            }
        } catch (Exception e) {
            System.err.println("Failed to parse line: " + e.getMessage());
        }
        return null;
    }
    
    private void assignDeviceInfo(EnergyRecord record) {
        // Assign devices based on energy usage pattern
        double energy = record.getGridEnergy();
        
        if (energy < 100) {
            record.setDeviceId("DEV-001");
            record.setDeviceType("LED_Light");
        } else if (energy < 300) {
            record.setDeviceId("DEV-002");
            record.setDeviceType("Laptop");
        } else if (energy < 600) {
            record.setDeviceId("DEV-003");
            record.setDeviceType("Refrigerator");
        } else {
            record.setDeviceId("DEV-004");
            record.setDeviceType("Air_Conditioner");
        }
    }
    
    private String formatForUDP(EnergyRecord record) {
        // Format: deviceId|deviceType|activePower|totalEnergy|timestamp
        return String.format("%s|%s|%.2f|%.2f|%s",
            record.getDeviceId(),
            record.getDeviceType(),
            record.getActivePower(),
            record.getGridEnergy(),
            record.getTimestamp().toString()
        );
    }
    
    private void sendMessage(String message) {
        try {
            byte[] sendData = message.getBytes();
            DatagramPacket packet = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Failed to send message: " + e.getMessage());
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
        
        // Path to CSV file (adjust as needed)
        String csvPath = "data/iiot_smart_grid_dataset.csv";
        
        client.sendLiveFeed(csvPath);
    }
}