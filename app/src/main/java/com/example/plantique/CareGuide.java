package com.example.plantique;

public class CareGuide {
    private String watering;
    private String lighting;
    private String soil;
    private String humidity;
    private String fertilizing;
    private String temperature;

    // Empty constructor needed for Firebase
    public CareGuide() {
    }

    public CareGuide(String watering, String lighting, String soil, String humidity, String fertilizing, String temperature) {
        this.watering = watering;
        this.lighting = lighting;
        this.soil = soil;
        this.humidity = humidity;
        this.fertilizing = fertilizing;
        this.temperature = temperature;
    }

    public String getWatering() {
        return watering;
    }

    public void setWatering(String watering) {
        this.watering = watering;
    }

    public String getLighting() {
        return lighting;
    }

    public void setLighting(String lighting) {
        this.lighting = lighting;
    }

    public String getSoil() {
        return soil;
    }

    public void setSoil(String soil) {
        this.soil = soil;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getFertilizing() {
        return fertilizing;
    }

    public void setFertilizing(String fertilizing) {
        this.fertilizing = fertilizing;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
}