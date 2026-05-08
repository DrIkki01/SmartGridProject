package com.smartgrid.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EnergyRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Time fields
    private LocalDateTime timestamp;
    private int hour;
    private int dayOfWeek;
    private String month;
    
    // Power measurements
    private double activePower;      // kW
    private double reactivePower;    // kVAR
    private double voltage;          // V
    private double current;          // A
    
    // Grid parameters
    private double gridFrequency;    // Hz
    private double powerFactor;
    
    // Energy sources
    private double gridEnergy;       // kWh
    private double solarEnergy;      // kWh
    private double windEnergy;       // kWh
    private double hybridEnergy;     // kWh
    
    // Environmental
    private double temperature;      // °C
    private double humidity;         // %
    private String weatherCondition;
    
    // Target variable
    private int energyEfficiencyScore; // 0-100
    
    // Device info (for your analysis)
    private String deviceId;
    private String deviceType;
    
    // Constructors
    public EnergyRecord() {}
    
    // Getters and Setters
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }
    
    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    
    public double getActivePower() { return activePower; }
    public void setActivePower(double activePower) { this.activePower = activePower; }
    
    public double getReactivePower() { return reactivePower; }
    public void setReactivePower(double reactivePower) { this.reactivePower = reactivePower; }
    
    public double getVoltage() { return voltage; }
    public void setVoltage(double voltage) { this.voltage = voltage; }
    
    public double getCurrent() { return current; }
    public void setCurrent(double current) { this.current = current; }
    
    public double getGridFrequency() { return gridFrequency; }
    public void setGridFrequency(double gridFrequency) { this.gridFrequency = gridFrequency; }
    
    public double getPowerFactor() { return powerFactor; }
    public void setPowerFactor(double powerFactor) { this.powerFactor = powerFactor; }
    
    public double getGridEnergy() { return gridEnergy; }
    public void setGridEnergy(double gridEnergy) { this.gridEnergy = gridEnergy; }
    
    public double getSolarEnergy() { return solarEnergy; }
    public void setSolarEnergy(double solarEnergy) { this.solarEnergy = solarEnergy; }
    
    public double getWindEnergy() { return windEnergy; }
    public void setWindEnergy(double windEnergy) { this.windEnergy = windEnergy; }
    
    public double getHybridEnergy() { return hybridEnergy; }
    public void setHybridEnergy(double hybridEnergy) { this.hybridEnergy = hybridEnergy; }
    
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    
    public double getHumidity() { return humidity; }
    public void setHumidity(double humidity) { this.humidity = humidity; }
    
    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }
    
    public int getEnergyEfficiencyScore() { return energyEfficiencyScore; }
    public void setEnergyEfficiencyScore(int energyEfficiencyScore) { this.energyEfficiencyScore = energyEfficiencyScore; }
    
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    
    // Helper method to get total energy from all sources
    public double getTotalEnergy() {
        return gridEnergy + solarEnergy + windEnergy + hybridEnergy;
    }
    
    @Override
    public String toString() {
        return String.format("EnergyRecord[timestamp=%s, device=%s, activePower=%.2f kW, efficiency=%d]",
            timestamp, deviceId, activePower, energyEfficiencyScore);
    }
}