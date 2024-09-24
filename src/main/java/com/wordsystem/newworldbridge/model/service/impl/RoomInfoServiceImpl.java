package com.wordsystem.newworldbridge.model.service.impl;

import com.wordsystem.newworldbridge.dto.RoomInfo;
import com.wordsystem.newworldbridge.model.dao.RoomInfoDao;
import com.wordsystem.newworldbridge.model.service.RoomInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomInfoServiceImpl implements RoomInfoService {

    private final RoomInfoDao roomInfoDao;

    @Autowired
    public RoomInfoServiceImpl(RoomInfoDao roomInfoDao) {
        this.roomInfoDao = roomInfoDao;
    }

    @Override
    public void updateRoom(RoomInfo roomInfo) {
        roomInfoDao.updateRoom(roomInfo);
    }

    @Override
    public void setRoom(RoomInfo roomInfo) {
        roomInfoDao.setRoom(roomInfo);
    }

    @Override
    public void deleteRoom(int id) {
        roomInfoDao.deleteRoom(id);
    }

    @Override
    public RoomInfo getRoom(int id) {
        return roomInfoDao.getRoom(id);
    }


    @Override
    public List<RoomInfo> getAllRooms() {
        return roomInfoDao.getAllRooms();
    }

    @Override
    public void setInGame(int id, int inGameStatus) {
        roomInfoDao.setInGame(id, inGameStatus);
    }
}
