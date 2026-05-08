package com.smartgrid.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class EnergyRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Time fields
    private LocalDateTime timestamp;
    private int hour;
    private int dayOfWeek;
    private String month;
    
    // Power measurements (from CSV columns)
    private double powerConsumption;      // Power_Consumption_kWh
    private double activePower;           // Active_Power_kW
    private double reactivePower;         // Reactive_Power_kVAR
    private double voltage;               // Voltage_V
    private double current;               // Current_A
    
    // Grid parameters
    private double gridFrequency;         // Grid_Frequency_Hz
    private double powerFactor;           // Power_Factor
    private int demandResponseEvent;      // Demand_Response_Event
    
    // Energy sources
    private double solarPowerGeneration;  // Solar_Power_Generation_kW
    private double windPowerGeneration;   // Wind_Power_Generation_kW
    private double previousDayConsumption; // Previous_Day_Consumption_kWh
    private int peakLoadHour;             // Peak_Load_Hour
    private int energySourceType;         // Energy_Source_Type
    private int userType;                 // User_Type
    private double normalizedConsumption; // Normalized_Consumption
    
    // Legacy energy fields (for compatibility)
    private double gridEnergy;
    private double solarEnergy;
    private double windEnergy;
    private double hybridEnergy;
    
    // Environmental
    private double temperature;           // Temperature_C
    private double humidity;              // Humidity_%
    private String weatherCondition;      // Weather_Condition
    
    // Target variable
    private int energyEfficiencyScore;    // Energy_Efficiency_Score
    
    // Device info
    private String deviceId;
    private String deviceType;
    
    // Constructors
    public EnergyRecord() {}
    
    // ========== GETTERS AND SETTERS ==========
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }
    
    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    
    public double getPowerConsumption() { return powerConsumption; }
    public void setPowerConsumption(double powerConsumption) { this.powerConsumption = powerConsumption; }
    
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
    
    public int getDemandResponseEvent() { return demandResponseEvent; }
    public void setDemandResponseEvent(int demandResponseEvent) { this.demandResponseEvent = demandResponseEvent; }
    
    public double getSolarPowerGeneration() { return solarPowerGeneration; }
    public void setSolarPowerGeneration(double solarPowerGeneration) { this.solarPowerGeneration = solarPowerGeneration; }
    
    public double getWindPowerGeneration() { return windPowerGeneration; }
    public void setWindPowerGeneration(double windPowerGeneration) { this.windPowerGeneration = windPowerGeneration; }
    
    public double getPreviousDayConsumption() { return previousDayConsumption; }
    public void setPreviousDayConsumption(double previousDayConsumption) { this.previousDayConsumption = previousDayConsumption; }
    
    public int getPeakLoadHour() { return peakLoadHour; }
    public void setPeakLoadHour(int peakLoadHour) { this.peakLoadHour = peakLoadHour; }
    
    public int getEnergySourceType() { return energySourceType; }
    public void setEnergySourceType(int energySourceType) { this.energySourceType = energySourceType; }
    
    public int getUserType() { return userType; }
    public void setUserType(int userType) { this.userType = userType; }
    
    public double getNormalizedConsumption() { return normalizedConsumption; }
    public void setNormalizedConsumption(double normalizedConsumption) { this.normalizedConsumption = normalizedConsumption; }
    
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
    
    // Helper method
    public double getTotalEnergy() {
        return gridEnergy + solarEnergy + windEnergy + hybridEnergy;
    }
    
    @Override
    public String toString() {
        return String.format("EnergyRecord[timestamp=%s, powerConsumption=%.2f kWh, efficiency=%d]",
            timestamp, powerConsumption, energyEfficiencyScore);
    }
}