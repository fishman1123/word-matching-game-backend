package com.wordsystem.newworldbridge.model.service.impl;

import com.wordsystem.newworldbridge.dto.Login;
import com.wordsystem.newworldbridge.model.dao.LoginDao;
import com.wordsystem.newworldbridge.model.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    private final LoginDao loginDao;

    @Autowired
    public LoginServiceImpl(LoginDao loginDao) {
        this.loginDao = loginDao;
    }

    @Override
    public void setUser(Login user) {
        loginDao.setUser(user);
    }

    @Override
    public Login getUser(int id) {
        return loginDao.getUser(id);
    }

    @Override
    public void updateUser(Login user) {
        loginDao.updateUser(user);
    }

    @Override
    public void deleteUser(int id) {
        loginDao.deleteUser(id);
    }

    @Override
    public int getId(String username) {
        return loginDao.getId(username);
    }

    @Override
    public void updateId(int id, int newId) {
        loginDao.updateId(id, newId);
    }

    @Override
    public void deleteId(int id) {
        loginDao.deleteId(id);
    }
}
