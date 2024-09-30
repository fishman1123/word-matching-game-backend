package com.wordsystem.newworldbridge.controller;

import com.wordsystem.newworldbridge.dto.Login;
import com.wordsystem.newworldbridge.dto.Score;
import com.wordsystem.newworldbridge.dto.UserInformation;
import com.wordsystem.newworldbridge.model.service.LoginService;
import com.wordsystem.newworldbridge.model.service.ScoreService;
import com.wordsystem.newworldbridge.model.service.UserInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserInfoController {

    @Autowired
    private UserInformationService userInformationService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private ScoreService scoreService;

    @GetMapping("/api/userinfo-here")
    public ResponseEntity<Map<String, Object>> getUserInfo(@AuthenticationPrincipal OAuth2User principal) {
        // Extract email and profile image from the OAuth2User principal
        String email = principal.getAttribute("email");
        String profileImage = principal.getAttribute("picture"); // Google provides the profile picture under 'picture' key

        Integer userId = loginService.getIdByEmail(email);
        if (userId == null) {
            return ResponseEntity.notFound().build();
        }

        UserInformation userInfo = userInformationService.getUserInformation(userId);
        if (userInfo == null) {
            return ResponseEntity.notFound().build();
        }

        Score userScore = scoreService.getScore(userId);

        Score tempScore;

        if (userScore == null) {
            tempScore = new Score();
            tempScore.setId(userId);
            tempScore.setUserScore(0);
        } else {
            tempScore = userScore;
        }


        // Create a response map to include the user info along with the profile image
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userInfo.getId());
        response.put("score", tempScore);
        response.put("email", email);
        response.put("profileImage", profileImage); // Include the Google profile image
        response.put("otherInfo", userInfo); // You can include any other relevant user information here

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/username")
    @Transactional
    public ResponseEntity<Map<String, String>> getUsername(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
        }

        String email = principal.getAttribute("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email is required"));
        }

        Integer userId = loginService.getIdByEmail(email);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }

        Login login = loginService.getUser(userId);
        if (login == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Login details not found"));
        }

        String username = login.getUsername();
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Username not set"));
        }

        Map<String, String> response = new HashMap<>();
        response.put("username", username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/user/{userId}")
    public ResponseEntity<UserInformation> getUserById(@PathVariable int userId) {
        try {
            UserInformation userInfo = userInformationService.getUserInformation(userId);
            if (userInfo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/api/user/{userId}/username")
    public ResponseEntity<Map<String, String>> getUsernameById(@PathVariable int userId) {
        try {
            Login login = loginService.getUser(userId);
            if (login == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
            }
            String username = login.getUsername();
            Map<String, String> response = new HashMap<>();
            response.put("username", username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // **New Endpoint to Update isUserPlaying Status**
    @PutMapping("/api/user/{userId}/isPlaying")
    public ResponseEntity<String> updateUserIsPlaying(@PathVariable int userId, @RequestBody Map<String, Integer> requestBody) {
        try {
            Integer isUserPlaying = requestBody.get("isUserPlaying");
            if (isUserPlaying == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: isUserPlaying not provided");
            }

            userInformationService.updateUserIsPlaying(userId, isUserPlaying);
            return ResponseEntity.ok("isUserPlaying status updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating isUserPlaying status: " + e.getMessage());
        }
    }
}
