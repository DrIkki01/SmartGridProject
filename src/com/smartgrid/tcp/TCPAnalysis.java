package com.smartgrid.tcp;

import com.smartgrid.model.EnergyRecord;
import java.io.*;
import java.util.*;

public class TCPAnalysis {
    
    public void analyze(List<EnergyRecord> records) {
        System.out.println("\n========================================");
        System.out.println("     TCP BATCH ANALYSIS REPORT");
        System.out.println("========================================");
        System.out.println("Total Records Analyzed: " + records.size());
        
        // Analysis 1: Total usage by device (using UserType as device category)
        Map<Integer, Double> totalUsageByDevice = new HashMap<>();
        Map<Integer, Integer> recordCountByDevice = new HashMap<>();
        
        for (EnergyRecord record : records) {
            int deviceType = record.getUserType(); // UserType 1 or 2
            double consumption = record.getPowerConsumption();
            
            totalUsageByDevice.put(deviceType, 
                totalUsageByDevice.getOrDefault(deviceType, 0.0) + consumption);
            recordCountByDevice.put(deviceType, 
                recordCountByDevice.getOrDefault(deviceType, 0) + 1);
        }
        
        System.out.println("\n--- Analysis 1: Total Usage by Device ---");
        for (Map.Entry<Integer, Double> entry : totalUsageByDevice.entrySet()) {
            int deviceId = entry.getKey();
            double total = entry.getValue();
            int count = recordCountByDevice.get(deviceId);
            double average = total / count;
            
            String deviceName = (deviceId == 1) ? "Residential User" : "Commercial User";
            System.out.printf("Device %d (%s):\n", deviceId, deviceName);
            System.out.printf("  Total Energy: %.2f kWh\n", total);
            System.out.printf("  Average Energy: %.2f kWh\n", average);
            System.out.printf("  Record Count: %d\n", count);
        }
        
        // Analysis 2: Device with highest number of events (using EnergySourceType as event indicator)
        Map<Integer, Integer> eventCountBySource = new HashMap<>();
        
        for (EnergyRecord record : records) {
            int sourceType = record.getEnergySourceType();
            eventCountBySource.put(sourceType, 
                eventCountBySource.getOrDefault(sourceType, 0) + 1);
        }
        
        System.out.println("\n--- Analysis 2: Device with Highest Number of Events ---");
        
        int highestSource = -1;
        int highestCount = 0;
        
        for (Map.Entry<Integer, Integer> entry : eventCountBySource.entrySet()) {
            int source = entry.getKey();
            int count = entry.getValue();
            
            String sourceName = "";
            switch (source) {
                case 1: sourceName = "Grid Only"; break;
                case 2: sourceName = "Solar + Grid"; break;
                case 3: sourceName = "Wind + Grid"; break;
                case 4: sourceName = "Hybrid (Solar+Wind+Grid)"; break;
                default: sourceName = "Unknown";
            }
            
            System.out.printf("Energy Source %d (%s): %d events\n", source, sourceName, count);
            
            if (count > highestCount) {
                highestCount = count;
                highestSource = source;
            }
        }
        
        String highestSourceName = "";
        switch (highestSource) {
            case 1: highestSourceName = "Grid Only"; break;
            case 2: highestSourceName = "Solar + Grid"; break;
            case 3: highestSourceName = "Wind + Grid"; break;
            case 4: highestSourceName = "Hybrid (Solar+Wind+Grid)"; break;
        }
        
        System.out.println("\n🏆 HIGHEST EVENTS: Energy Source " + highestSource + " (" + highestSourceName + ") with " + highestCount + " events");
        
        // Additional summary statistics
        double totalEnergy = 0;
        double maxEnergy = 0;
        double minEnergy = Double.MAX_VALUE;
        String maxTimestamp = "";
        
        for (EnergyRecord record : records) {
            double consumption = record.getPowerConsumption();
            totalEnergy += consumption;
            if (consumption > maxEnergy) {
                maxEnergy = consumption;
                maxTimestamp = record.getTimestamp().toString();
            }
            if (consumption < minEnergy) {
                minEnergy = consumption;
            }
        }
        
        double avgEnergy = totalEnergy / records.size();
        
        System.out.println("\n--- Additional Summary Statistics ---");
        System.out.printf("Total Energy Consumed: %.2f kWh\n", totalEnergy);
        System.out.printf("Average Energy: %.2f kWh\n", avgEnergy);
        System.out.printf("Max Energy: %.2f kWh at %s\n", maxEnergy, maxTimestamp);
        System.out.printf("Min Energy: %.2f kWh\n", minEnergy);
        
        // Save to file
        saveReportToFile(records, totalUsageByDevice, eventCountBySource, highestSource, highestCount, 
                         totalEnergy, avgEnergy, maxEnergy, maxTimestamp, minEnergy);
    }
    
    private void saveReportToFile(List<EnergyRecord> records, Map<Integer, Double> totalUsageByDevice,
                                   Map<Integer, Integer> eventCountBySource, int highestSource, int highestCount,
                                   double totalEnergy, double avgEnergy, double maxEnergy, String maxTimestamp, double minEnergy) {
        try {
            PrintWriter writer = new PrintWriter("TCP_Analysis_Report.txt");
            writer.println("========================================");
            writer.println("     TCP BATCH ANALYSIS REPORT");
            writer.println("========================================");
            writer.println("Total Records Analyzed: " + records.size());
            
            writer.println("\n--- Analysis 1: Total Usage by Device ---");
            for (Map.Entry<Integer, Double> entry : totalUsageByDevice.entrySet()) {
                int deviceId = entry.getKey();
                double total = entry.getValue();
                String deviceName = (deviceId == 1) ? "Residential User" : "Commercial User";
                writer.printf("Device %d (%s): Total Energy = %.2f kWh\n", deviceId, deviceName, total);
            }
            
            writer.println("\n--- Analysis 2: Device with Highest Number of Events ---");
            for (Map.Entry<Integer, Integer> entry : eventCountBySource.entrySet()) {
                writer.printf("Energy Source %d: %d events\n", entry.getKey(), entry.getValue());
            }
            writer.printf("HIGHEST: Energy Source %d with %d events\n", highestSource, highestCount);
            
            writer.println("\n--- Summary Statistics ---");
            writer.printf("Total Energy Consumed: %.2f kWh\n", totalEnergy);
            writer.printf("Average Energy: %.2f kWh\n", avgEnergy);
            writer.printf("Max Energy: %.2f kWh at %s\n", maxEnergy, maxTimestamp);
            writer.printf("Min Energy: %.2f kWh\n", minEnergy);
            
            writer.close();
            System.out.println("\n Report saved to: TCP_Analysis_Report.txt");
            
        } catch (FileNotFoundException e) {
            System.err.println("Failed to save report: " + e.getMessage());
        }
    }
}