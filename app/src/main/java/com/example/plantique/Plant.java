package com.example.plantique;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing a plant in the application
 */
public class Plant implements Parcelable {
    private String id;
    private String name;
    private String description;
    private String category;
    private String imageUrl;
    private String waterSchedule;
    private String sunlightNeed;
    private String soilMedia;
    private String humidity;
    private String fertilizing;
    private String temperature;
    private String source; // "admin" or "user"
    private String userId; // Added userId field
    private Map<String, Map<String, Object>> tips;

    /**
     * Empty constructor required for Firestore
     */
    public Plant() {
        // Required empty constructor for Firestore
    }

    /**
     * Constructor with basic plant details
     */
    public Plant(String id, String name, String description, String category, String imageUrl, String source) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.source = source;
        this.tips = new HashMap<>();
    }

    /**
     * Full constructor with all plant details
     */
    public Plant(String id, String name, String description, String category, String imageUrl,
                 String waterSchedule, String sunlightNeed, String soilMedia, String humidity,
                 String fertilizing, String temperature, String source) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.waterSchedule = waterSchedule;
        this.sunlightNeed = sunlightNeed;
        this.soilMedia = soilMedia;
        this.humidity = humidity;
        this.fertilizing = fertilizing;
        this.temperature = temperature;
        this.source = source;
        this.tips = new HashMap<>();
    }

    /**
     * Checks if this plant is an admin plant
     * @return true if the plant's source is "admin", false otherwise
     */
    public boolean isAdminPlant() {
        return "admin".equals(this.source);
    }

    /**
     * Checks if this plant is a user plant
     * @return true if the plant's source is "user", false otherwise
     */
    public boolean isUserPlant() {
        return "user".equals(this.source);
    }

    public boolean hasValidData() {
        return name != null && !name.trim().isEmpty() &&
                (imageUrl != null && !imageUrl.trim().isEmpty());
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getWaterSchedule() {
        return waterSchedule;
    }

    public void setWaterSchedule(String waterSchedule) {
        this.waterSchedule = waterSchedule;
    }

    public String getSunlightNeed() {
        return sunlightNeed;
    }

    public void setSunlightNeed(String sunlightNeed) {
        this.sunlightNeed = sunlightNeed;
    }

    public String getSoilMedia() {
        return soilMedia;
    }

    public void setSoilMedia(String soilMedia) {
        this.soilMedia = soilMedia;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    // Added getter and setter for userId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, Map<String, Object>> getTips() {
        return tips;
    }

    public void setTips(Map<String, Map<String, Object>> tips) {
        this.tips = tips;
    }

    // Parcelable implementation
    protected Plant(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        category = in.readString();
        imageUrl = in.readString();
        waterSchedule = in.readString();
        sunlightNeed = in.readString();
        soilMedia = in.readString();
        humidity = in.readString();
        fertilizing = in.readString();
        temperature = in.readString();
        source = in.readString();
        userId = in.readString(); // Added userId to Parcelable implementation
    }

    public static final Creator<Plant> CREATOR = new Creator<Plant>() {
        @Override
        public Plant createFromParcel(Parcel in) {
            return new Plant(in);
        }

        @Override
        public Plant[] newArray(int size) {
            return new Plant[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(category);
        dest.writeString(imageUrl);
        dest.writeString(waterSchedule);
        dest.writeString(sunlightNeed);
        dest.writeString(soilMedia);
        dest.writeString(humidity);
        dest.writeString(fertilizing);
        dest.writeString(temperature);
        dest.writeString(source);
        dest.writeString(userId); // Added userId to writeToParcel
    }
}