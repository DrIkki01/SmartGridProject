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
    
    // Constructor with IP parameter (for GUI)
    public TCPClient(String serverIP) {
        this.serverIP = serverIP;
        try {
            socket = new Socket(serverIP, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("TCP Client connected to " + serverIP + ":" + SERVER_PORT);
        } catch (IOException e) {
            System.err.println("Failed to connect: " + e.getMessage());
        }
    }
    
    // Default constructor for backward compatibility
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
            
            System.out.println("Reading CSV file...");
            int lineNumber = 0;
            int successCount = 0;
            
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(",");
                
                if (parts.length >= 20) {
                    try {
                        EnergyRecord record = new EnergyRecord();
                        
                        record.setTimestamp(java.time.LocalDateTime.parse(parts[0].replace(" ", "T")));
                        record.setPowerConsumption(Double.parseDouble(parts[1].trim()));
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
                        successCount++;
                        
                    } catch (NumberFormatException e) {
                        // Silent skip for parsing errors
                    }
                }
            }
            br.close();
            
            System.out.println("\nRecords loaded: " + records.size());
            
            if (records.isEmpty()) {
                System.err.println("No records loaded! Check CSV format.");
                return;
            }
            
            System.out.println("\nSending " + records.size() + " records to server...");
            
            out.writeInt(records.size());
            out.flush();
            
            int sent = 0;
            for (EnergyRecord record : records) {
                out.writeObject(record);
                out.flush();
                sent++;
                if (sent % 1000 == 0) {
                    System.out.println("Sent " + sent + "/" + records.size() + " records...");
                }
            }
            
            out.writeObject("END_OF_BATCH");
            out.flush();
            
            System.out.println("Batch transfer complete! " + records.size() + " records sent.");
            
        } catch (Exception e) {
            System.err.println("Error sending batch: " + e.getMessage());
            e.printStackTrace();
        } finally {
            close();
        }
    }
    
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