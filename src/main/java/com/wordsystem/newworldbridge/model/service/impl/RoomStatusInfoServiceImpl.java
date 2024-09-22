// src/model/service/impl/RoomStatusInfoServiceImpl.java
package com.wordsystem.newworldbridge.model.service.impl;

import com.wordsystem.newworldbridge.dto.RoomStatusInfo;
import com.wordsystem.newworldbridge.model.dao.RoomStatusInfoDao;
import com.wordsystem.newworldbridge.model.service.RoomStatusInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoomStatusInfoServiceImpl implements RoomStatusInfoService {

    private final RoomStatusInfoDao roomStatusInfoDao;

    @Autowired
    public RoomStatusInfoServiceImpl(RoomStatusInfoDao roomStatusInfoDao) {
        this.roomStatusInfoDao = roomStatusInfoDao;
    }

    @Override
    public void insertRoomStatusInfo(RoomStatusInfo roomStatusInfo) {
        roomStatusInfoDao.insertRoomStatusInfo(roomStatusInfo);
    }

    @Override
    public RoomStatusInfo getRoomStatusInfoById(Integer id) {
        return roomStatusInfoDao.getRoomStatusInfoById(id);
    }

    @Override
    public void updateRoomStatusInfo(RoomStatusInfo roomStatusInfo) {
        roomStatusInfoDao.updateRoomStatusInfo(roomStatusInfo);
    }

    @Override
    public void deleteRoomStatusInfoById(Integer id) {
        roomStatusInfoDao.deleteRoomStatusInfoById(id);
    }
}
