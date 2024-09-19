package com.wordsystem.newworldbridge.dto;

public class UserInformation {
    private int id;
    private int userWin;
    private int userLose;
    private int userHasRoom;
    private int isUserPlaying;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserWin() { return userWin; }
    public void setUserWin(int userWin) { this.userWin = userWin; }

    public int getUserLose() { return userLose; }
    public void setUserLose(int userLose) { this.userLose = userLose; }

    public int getUserHasRoom() { return userHasRoom; }
    public void setUserHasRoom(int userHasRoom) { this.userHasRoom = userHasRoom; }

    public int getIsUserPlaying() { return isUserPlaying; }
    public void setIsUserPlaying(int isUserPlaying) { this.isUserPlaying = isUserPlaying; }
}
