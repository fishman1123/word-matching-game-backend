// src/main/java/com/wordsystem/newworldbridge/controller/ScoreController.java

package com.wordsystem.newworldbridge.controller;

import com.wordsystem.newworldbridge.dto.Score;
import com.wordsystem.newworldbridge.model.service.ScoreService;
import com.wordsystem.newworldbridge.model.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private LoginService loginService;


    @PostMapping("/score/update")
    public ResponseEntity<String> updateWinnerScore(@RequestBody Map<String, Object> requestBody) {
        try {
            Object userIdObj = requestBody.get("userId");
            if (userIdObj == null) {
                return ResponseEntity.badRequest().body("Missing 'userId' in request body.");
            }

            int userId;
            if (userIdObj instanceof Number) {
                userId = ((Number) userIdObj).intValue();
            } else {
                // Attempt to parse if it's a string
                try {
                    userId = Integer.parseInt(userIdObj.toString());
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body("'userId' must be a number.");
                }
            }

            System.out.println("Received request to update score for userId: " + userId);

            // Check if score exists
            Score existingScore = scoreService.getScore(userId);
            if (existingScore != null) {
                // Update existing score by incrementing by 1
                System.out.println("Checking score: " + existingScore.getUserScore());
                int newScore = existingScore.getUserScore() + 1;
                System.out.println("Updating score for userId " + userId + " to " + newScore);
                scoreService.updateScore(userId, newScore);
            } else {
                // Create new score record with score = 1
                Score newScore = new Score();
                newScore.setId(userId);
                newScore.setUserScore(1);
                System.out.println("Creating new score record for userId " + userId);
                scoreService.setScore(newScore);
            }
            return ResponseEntity.ok("Score updated successfully.");
        } catch (Exception e) {
            System.err.println("Error updating score: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error updating score: " + e.getMessage());
        }
    }

    /**
     * Endpoint to retrieve all scores.
     * Returns a list of users with their usernames and scores.
     */
    @GetMapping("/scores")
    public ResponseEntity<List<Map<String, Object>>> getAllScores() {
        try {
            List<Score> scores = scoreService.getAllScores();
            List<Map<String, Object>> scoresWithUsernames = new ArrayList<>();

            for (Score score : scores) {
                String username = loginService.getUserNameById(score.getId());
                Map<String, Object> scoreMap = new HashMap<>();
                scoreMap.put("userId", score.getId());
                scoreMap.put("username", username != null ? username : "Unknown");
                scoreMap.put("userScore", score.getUserScore());
                scoresWithUsernames.add(scoreMap);
            }

            return ResponseEntity.ok(scoresWithUsernames);
        } catch (Exception e) {
            System.err.println("Error retrieving scores: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/score/{userId}")
    public ResponseEntity<Map<String, Object>> getScoreById(@PathVariable int userId) {
        Map<String, Object> scoreMap = new HashMap<>();
        try {
            String username = loginService.getUserNameById(userId);
            Score score = scoreService.getScore(userId);
            if (score == null) {
                scoreMap.put("userId", userId);
                scoreMap.put("username", username != null ? username : "Unknown");
                scoreMap.put("userScore", 0);
                return ResponseEntity.ok(scoreMap);
            }


            scoreMap.put("userId", score.getId());
            scoreMap.put("username", username != null ? username : "Unknown");
            scoreMap.put("userScore", score.getUserScore());

            return ResponseEntity.ok(scoreMap);
        } catch (Exception e) {
            System.err.println("Error retrieving score: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}
