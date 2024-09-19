package com.wordsystem.newworldbridge.dto;

public class Chat {
    private int chatId;
    private int userSenderId;
    private int userReceiverId;
    private String chatLogUrl;

    // Getters and Setters
    public int getChatId() { return chatId; }
    public void setChatId(int chatId) { this.chatId = chatId; }

    public int getUserSenderId() { return userSenderId; }
    public void setUserSenderId(int userSenderId) { this.userSenderId = userSenderId; }

    public int getUserReceiverId() { return userReceiverId; }
    public void setUserReceiverId(int userReceiverId) { this.userReceiverId = userReceiverId; }

    public String getChatLogUrl() { return chatLogUrl; }
    public void setChatLogUrl(String chatLogUrl) { this.chatLogUrl = chatLogUrl; }
}
