package com.wordsystem.newworldbridge.dto;

public class RoomInfo {
    private int id;
    private int inGame;
    private double roomLocationLongitude;
    private double roomLocationLatitude;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getInGame() { return inGame; }
    public void setInGame(int inGame) { this.inGame = inGame; }

    public double getRoomLocationLongitude() { return roomLocationLongitude; }
    public void setRoomLocationLongitude(double roomLocationLongitude) { this.roomLocationLongitude = roomLocationLongitude; }

    public double getRoomLocationLatitude() { return roomLocationLatitude; }
    public void setRoomLocationLatitude(double roomLocationLatitude) { this.roomLocationLatitude = roomLocationLatitude; }
}
