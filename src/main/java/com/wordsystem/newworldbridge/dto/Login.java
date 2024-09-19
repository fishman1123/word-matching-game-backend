package com.wordsystem.newworldbridge.dto;

public class Login {
    private int id;
    private String socialEmail;
    private String username;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSocialEmail() { return socialEmail; }
    public void setSocialEmail(String socialEmail) { this.socialEmail = socialEmail; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
