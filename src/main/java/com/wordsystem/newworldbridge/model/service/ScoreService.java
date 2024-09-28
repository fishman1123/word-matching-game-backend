// src/main/java/com/wordsystem/newworldbridge/model/service/ScoreService.java

package com.wordsystem.newworldbridge.model.service;

import com.wordsystem.newworldbridge.dto.Score;

import java.util.List;

public interface ScoreService {

    // Set Score
    void setScore(Score score);

    // Update Score
    void updateScore(int id, int newScore);

    // Delete Score
    void deleteScore(int id);

    // Get Score by ID
    Score getScore(int id);

    // Get All Scores
    List<Score> getAllScores();
}
