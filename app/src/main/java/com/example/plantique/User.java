package com.example.plantique;

/**
 * Model class representing a user in the application
 */
public class User {
    private String id;
    private String fullName;
    private String username;
    private String email;
    private String bio;
    private String photoUrl;
    private String userType; // "regular" or "admin"

    /**
     * Empty constructor required for Firestore
     */
    public User() {
        // Required empty constructor for Firestore
    }

    /**
     * Constructor with basic user details
     */
    public User(String id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.userType = "regular"; // Default user type
    }

    /**
     * Full constructor with all user details
     */
    public User(String id, String fullName, String username, String email, String bio, String photoUrl, String userType) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.photoUrl = photoUrl;
        this.userType = userType;
    }

    /**
     * Checks if this user is an admin user
     * @return true if the user's type is "admin", false otherwise
     */
    public boolean isAdmin() {
        return "admin".equals(this.userType);
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}