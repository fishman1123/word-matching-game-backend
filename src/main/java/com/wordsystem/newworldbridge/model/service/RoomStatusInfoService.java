// src/model/service/RoomStatusInfoService.java
package com.wordsystem.newworldbridge.model.service;

import com.wordsystem.newworldbridge.dto.RoomStatusInfo;

public interface RoomStatusInfoService {

    void insertRoomStatusInfo(RoomStatusInfo roomStatusInfo);

    RoomStatusInfo getRoomStatusInfoById(Integer id);

    void updateRoomStatusInfo(RoomStatusInfo roomStatusInfo);

    void deleteRoomStatusInfoById(Integer id);
}
