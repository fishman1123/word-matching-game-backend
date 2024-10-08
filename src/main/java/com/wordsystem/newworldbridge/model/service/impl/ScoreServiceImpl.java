// src/main/java/com/wordsystem/newworldbridge/model/service/impl/ScoreServiceImpl.java

package com.wordsystem.newworldbridge.model.service.impl;

import com.wordsystem.newworldbridge.dto.Score;
import com.wordsystem.newworldbridge.model.dao.ScoreDao;
import com.wordsystem.newworldbridge.model.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoreServiceImpl implements ScoreService {

    private final ScoreDao scoreDao;

    @Autowired
    public ScoreServiceImpl(ScoreDao scoreDao) {
        this.scoreDao = scoreDao;
    }

    @Override
    public void setScore(Score score) {
        scoreDao.setScore(score);
    }

    @Override
    public void updateScore(int id, int newScore) {
        scoreDao.updateScore(id, newScore);
    }

    @Override
    public void deleteScore(int id) {
        scoreDao.deleteScore(id);
    }

    @Override
    public Score getScore(int id) {
        return scoreDao.getScore(id);
    }

    @Override
    public List<Score> getAllScores() {
        return scoreDao.getAllScores();
    }
}
