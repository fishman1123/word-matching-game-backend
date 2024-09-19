package com.wordsystem.newworldbridge.model.service.impl;

import com.wordsystem.newworldbridge.dto.UserInformation;
import com.wordsystem.newworldbridge.model.dao.UserInformationDao;
import com.wordsystem.newworldbridge.model.service.UserInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserInformationServiceImpl implements UserInformationService {

    private final UserInformationDao userInformationDao;

    @Autowired
    public UserInformationServiceImpl(UserInformationDao userInformationDao) {
        this.userInformationDao = userInformationDao;
    }

    @Override
    public void updateUserWin(int id, int userWin) {
        userInformationDao.updateUserWin(id, userWin);
    }

    @Override
    public void updateUserLose(int id, int userLose) {
        userInformationDao.updateUserLose(id, userLose);
    }

    @Override
    public void updateUserHasRoom(int id, int userHasRoom) {
        userInformationDao.updateUserHasRoom(id, userHasRoom);
    }

    @Override
    public void updateUserIsPlaying(int id, int isUserPlaying) {
        userInformationDao.updateUserIsPlaying(id, isUserPlaying);
    }

    @Override
    public void setUserInformation(UserInformation userInfo) {
        userInformationDao.setUserInformation(userInfo);
    }

    @Override
    public UserInformation getUserInformation(int id) {
        return userInformationDao.getUserInformation(id);
    }

    @Override
    public void deleteUserInformation(int id) {
        userInformationDao.deleteUserInformation(id);
    }
}
