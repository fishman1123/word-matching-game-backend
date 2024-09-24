package com.wordsystem.newworldbridge.model.service;


import com.wordsystem.newworldbridge.dto.Login;

public interface LoginService {

    // Set User
    void setUser(Login user);

    // Get User
    Login getUser(int id);

    // Update User
    void updateUser(Login user);

    // Delete User
    void deleteUser(int id);

    // Get ID by Username
    int getId(String username);

    // Update ID
    void updateId(int id, int newId);

    // Delete ID (Same as deleteUser)
    void deleteId(int id);

    Integer getIdByEmail(String email);

    String getUserNameByEmail(String email);

    String getUserNameById(int id);

    void updateUsernameByEmail(String email, String username);
}
