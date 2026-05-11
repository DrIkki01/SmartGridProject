package com.smartgrid.tcp;

import com.smartgrid.model.EnergyRecord;
import java.io.*;
import java.net.*;
import java.util.*;

public class TCPClient {
    private static final int SERVER_PORT = 8888;
    
    private Socket socket;
    private ObjectOutputStream out;
    private String serverIP;
    
    // Validation counters
    private int totalRowsRead = 0;
    private int validRecords = 0;
    private int duplicateRecords = 0;
    private int malformedRecords = 0;
    private int outOfRangeRecords = 0;
    private Set<String> seenTimestamps;
    
    public TCPClient(String serverIP) {
        this.serverIP = serverIP;
        this.seenTimestamps = new HashSet<>();
        try {
            socket = new Socket(serverIP, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("TCP Client connected to " + serverIP + ":" + SERVER_PORT);
        } catch (IOException e) {
            System.err.println("Failed to connect: " + e.getMessage());
        }
    }
    
    public TCPClient() {
        this("localhost");
    }
    
    public void sendBatchData(String csvFilePath) {
        try {
            List<EnergyRecord> records = new ArrayList<>();
            
            FileInputStream fis = new FileInputStream(csvFilePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            
            String line;
            String header = br.readLine();
            if (header != null && header.startsWith("\uFEFF")) {
                header = header.substring(1);
            }
            System.out.println("Header: " + header);
            System.out.println("Starting data validation and batch transfer...");
            System.out.println("----------------------------------------");
            
            while ((line = br.readLine()) != null) {
                totalRowsRead++;
                
                if (line.trim().isEmpty()) {
                    System.out.println("[VALIDATION] Line " + totalRowsRead + ": Empty line - SKIPPED");
                    malformedRecords++;
                    continue;
                }
                
                String[] parts = line.split(",");
                
                // Check for malformed (not enough columns)
                if (parts.length < 20) {
                    System.out.println("[VALIDATION] Line " + totalRowsRead + ": Malformed - only " + parts.length + " columns (expected 20) - SKIPPED");
                    malformedRecords++;
                    continue;
                }
                
                try {
                    String timestamp = parts[0];
                    
                    // Check for duplicate timestamp
                    if (seenTimestamps.contains(timestamp)) {
                        System.out.println("[VALIDATION] Line " + totalRowsRead + ": Duplicate timestamp " + timestamp + " - SKIPPED");
                        duplicateRecords++;
                        continue;
                    }
                    
                    double powerConsumption = Double.parseDouble(parts[1].trim());
                    
                    // Range checking (0-10 kWh is normal range for this dataset)
                    if (powerConsumption < 0 || powerConsumption > 10) {
                        System.out.println("[VALIDATION] Line " + totalRowsRead + ": Power out of range (0-10 kWh): " + powerConsumption + " kWh - SKIPPED");
                        outOfRangeRecords++;
                        continue;
                    }
                    
                    // If all validation passes, create record
                    EnergyRecord record = new EnergyRecord();
                    record.setTimestamp(java.time.LocalDateTime.parse(parts[0].replace(" ", "T")));
                    record.setPowerConsumption(powerConsumption);
                    record.setVoltage(Double.parseDouble(parts[2].trim()));
                    record.setCurrent(Double.parseDouble(parts[3].trim()));
                    record.setPowerFactor(Double.parseDouble(parts[4].trim()));
                    record.setGridFrequency(Double.parseDouble(parts[5].trim()));
                    record.setReactivePower(Double.parseDouble(parts[6].trim()));
                    record.setActivePower(Double.parseDouble(parts[7].trim()));
                    record.setDemandResponseEvent((int)Double.parseDouble(parts[8].trim()));
                    record.setTemperature(Double.parseDouble(parts[9].trim()));
                    record.setHumidity(Double.parseDouble(parts[10].trim()));
                    record.setWeatherCondition(parts[11].trim());
                    record.setSolarPowerGeneration(Double.parseDouble(parts[12].trim()));
                    record.setWindPowerGeneration(Double.parseDouble(parts[13].trim()));
                    record.setPreviousDayConsumption(Double.parseDouble(parts[14].trim()));
                    record.setPeakLoadHour((int)Double.parseDouble(parts[15].trim()));
                    record.setEnergySourceType((int)Double.parseDouble(parts[16].trim()));
                    record.setUserType((int)Double.parseDouble(parts[17].trim()));
                    record.setNormalizedConsumption(Double.parseDouble(parts[18].trim()));
                    record.setEnergyEfficiencyScore((int)Double.parseDouble(parts[19].trim()));
                    
                    records.add(record);
                    validRecords++;
                    seenTimestamps.add(timestamp);
                    
                    if (validRecords % 1000 == 0) {
                        System.out.println("[VALIDATION] Processed " + validRecords + " valid records so far...");
                    }
                    
                } catch (NumberFormatException e) {
                    System.out.println("[VALIDATION] Line " + totalRowsRead + ": Invalid number format - " + e.getMessage() + " - SKIPPED");
                    malformedRecords++;
                } catch (Exception e) {
                    System.out.println("[VALIDATION] Line " + totalRowsRead + ": Parse error - " + e.getMessage() + " - SKIPPED");
                    malformedRecords++;
                }
            }
            br.close();
            
            // Print validation summary
            System.out.println("\n========== DATA VALIDATION SUMMARY ==========");
            System.out.println("Total rows read: " + totalRowsRead);
            System.out.println("Valid records: " + validRecords);
            System.out.println("Duplicate records (skipped): " + duplicateRecords);
            System.out.println("Malformed records (skipped): " + malformedRecords);
            System.out.println("Out of range records (skipped): " + outOfRangeRecords);
            System.out.println("=============================================\n");
            
            if (validRecords == 0) {
                System.err.println("No valid records to send!");
                return;
            }
            
            System.out.println("Sending " + validRecords + " valid records to server...");
            
            out.writeInt(validRecords);
            out.flush();
            
            int sent = 0;
            for (EnergyRecord record : records) {
                out.writeObject(record);
                out.flush();
                sent++;
                if (sent % 1000 == 0) {
                    System.out.println("Sent " + sent + "/" + validRecords + " records...");
                }
            }
            
            out.writeObject("END_OF_BATCH");
            out.flush();
            
            System.out.println("\nBatch transfer complete! " + validRecords + " records sent.");
            System.out.println("Validation stats: " + duplicateRecords + " duplicates, " + malformedRecords + " malformed, " + outOfRangeRecords + " out of range");
            
        } catch (Exception e) {
            System.err.println("Error sending batch: " + e.getMessage());
            e.printStackTrace();
        } finally {
            close();
        }
    }
    
    public int getValidRecords() { return validRecords; }
    public int getDuplicateRecords() { return duplicateRecords; }
    public int getMalformedRecords() { return malformedRecords; }
    public int getOutOfRangeRecords() { return outOfRangeRecords; }
    
    private void close() {
        try {
            if (out != null) out.close();
            if (socket != null) socket.close();
            System.out.println("TCP Client closed.");
        } catch (IOException e) {
            System.err.println("Error closing: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        TCPClient client = new TCPClient();
        client.sendBatchData("data/iiot_smart_grid_dataset.csv");
    }
}