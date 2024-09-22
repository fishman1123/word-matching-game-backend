// src/dto/RoomStatusInfo.java
package com.wordsystem.newworldbridge.dto;

public class RoomStatusInfo {
    private Integer id;
    private Integer enteredPlayerId;
    private Integer hostIsReady;
    private Integer visitorIsReady;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEnteredPlayerId() {
        return enteredPlayerId;
    }

    public void setEnteredPlayerId(Integer enteredPlayerId) {
        this.enteredPlayerId = enteredPlayerId;
    }

    public Integer getHostIsReady() {
        return hostIsReady;
    }

    public void setHostIsReady(Integer hostIsReady) {
        this.hostIsReady = hostIsReady;
    }

    public Integer getVisitorIsReady() {
        return visitorIsReady;
    }

    public void setVisitorIsReady(Integer visitorIsReady) {
        this.visitorIsReady = visitorIsReady;
    }
}
