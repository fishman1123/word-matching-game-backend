package com.wordsystem.newworldbridge.model.service;

import com.wordsystem.newworldbridge.dto.RoomInfo;

public interface RoomInfoService {

    // 13. setRoom
    void setRoom(RoomInfo roomInfo);

    // 14. deleteRoom
    void deleteRoom(int id);

    // 15. getRoom
    RoomInfo getRoom(int id);

    // 16. setInGame
    void setInGame(int id, int inGameStatus);
}