package com.wordsystem.newworldbridge.model.service;

import com.wordsystem.newworldbridge.dto.Score;

public interface ScoreService {

    // 17. setScore
    void setScore(Score score);

    // 18. updateScore
    void updateScore(int id, int newScore);

    // 19. deleteScore
    void deleteScore(int id);
}
