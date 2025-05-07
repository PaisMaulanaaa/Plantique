package com.example.plantique;

public class FeedPost {
    private String id;
    private String userId;
    private String username;
    private String userAvatar;
    private String plantName;
    private String plantImageUrl;
    private String description;
    private long timestamp;

    // Default constructor for Firebase
    public FeedPost() {
        // Required empty constructor for Firebase
    }

    // Constructor
    public FeedPost(String id, String userId, String username, String userAvatar, String plantName,
                            String plantImageUrl, String description, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.userAvatar = userAvatar;
        this.plantName = plantName;
        this.plantImageUrl = plantImageUrl;
        this.description = description;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public String getPlantImageUrl() {
        return plantImageUrl;
    }

    public void setPlantImageUrl(String plantImageUrl) {
        this.plantImageUrl = plantImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}