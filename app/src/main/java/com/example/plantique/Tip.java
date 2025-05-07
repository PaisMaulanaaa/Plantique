package com.example.plantique;

public class Tip {
    private String id;
    private String title;
    private String description;
    private String icon;
    private String iconColor;

    // Default constructor for Firebase
    public Tip() {
        // Required empty constructor for Firebase
    }

    // Constructor
    public Tip(String id, String title, String description, String icon, String iconColor) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.iconColor = iconColor;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIconColor() {
        return iconColor;
    }

    public void setIconColor(String iconColor) {
        this.iconColor = iconColor;
    }
}