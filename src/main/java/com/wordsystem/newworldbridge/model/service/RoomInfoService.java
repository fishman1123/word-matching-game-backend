package com.wordsystem.newworldbridge.model.service;

import com.wordsystem.newworldbridge.dto.RoomInfo;

import java.util.List;

public interface RoomInfoService {

    // 13. setRoom
    void setRoom(RoomInfo roomInfo);

    // 14. deleteRoom
    void deleteRoom(int id);

    // 15. getRoom
    RoomInfo getRoom(int id);

    void updateRoom(RoomInfo roomInfo);

    List<RoomInfo> getAllRooms();

    // 16. setInGame
    void setInGame(int id, int inGameStatus);
}