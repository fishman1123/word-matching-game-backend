package com.wordsystem.newworldbridge.model.service;

import com.wordsystem.newworldbridge.dto.UserInformation;

public interface UserInformationService {

    // Update User Win
    void updateUserWin(int id, int userWin);

    // Update User Lose
    void updateUserLose(int id, int userLose);

    // Update User Has Room
    void updateUserHasRoom(int id, int userHasRoom);

    // Update User Is Playing
    void updateUserIsPlaying(int id, int isUserPlaying);

    // Set User Information
    void setUserInformation(UserInformation userInfo);

    // Get User Information
    UserInformation getUserInformation(int id);

    // Delete User Information
    void deleteUserInformation(int id);
}
