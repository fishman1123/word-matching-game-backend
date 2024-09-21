package com.wordsystem.newworldbridge.controller;

import com.wordsystem.newworldbridge.dto.Login;
import com.wordsystem.newworldbridge.dto.UserInformation;
import com.wordsystem.newworldbridge.model.service.LoginService;
import com.wordsystem.newworldbridge.model.service.UserInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserInfoController {

    @Autowired
    private UserInformationService userInformationService;
    @Autowired
    private LoginService loginService;

    @GetMapping("/api/userinfo-here")
    public ResponseEntity<UserInformation> getUserInfo(@AuthenticationPrincipal OAuth2User principal) {
        // Get the email from the OAuth2User principal
        String email = principal.getAttribute("email");

        // Find the user's login information by email (assuming loginService is available)
        Integer userId = loginService.getIdByEmail(email);
        System.out.println("this is user id: " + userId);
        if (userId == null) {
            return ResponseEntity.notFound().build();
        }

        // Fetch user information from the user_information table using the user ID
        UserInformation userInfo = userInformationService.getUserInformation(userId);

        if (userInfo == null) {
            return ResponseEntity.notFound().build();
        }

        // Return the user information to the client
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/api/username")
    @Transactional  // Ensures atomicity if multiple database operations are needed
    public ResponseEntity<Map<String, String>> getUsername(@AuthenticationPrincipal OAuth2User principal) {
        System.out.println("Received request to /api/username");

        if (principal == null) {
            System.out.println("Principal is null. User might not be authenticated.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
        }

        // Get the email from the OAuth2User principal
        String email = principal.getAttribute("email");
        System.out.println("Authenticated user's email: " + email);

        if (email == null || email.isEmpty()) {
            System.out.println("Email attribute is missing in the principal.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email is required"));
        }

        // Find the user's login information by email
        Integer userId = loginService.getIdByEmail(email);
        System.out.println("Retrieved user ID from login table: " + userId);

        if (userId == null) {
            System.out.println("User with email " + email + " does not exist in the login table.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }

        // Fetch username from login table
        Login login = loginService.getUser(userId);
        if (login == null) {
            System.out.println("Login details not found for user ID " + userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Login details not found"));
        }

        String username = login.getUsername();
        System.out.println("Fetched username: " + username);

        if (username == null || username.isEmpty()) {
            System.out.println("Username not set for user ID " + userId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Username not set"));
        }

        // Return the username as JSON
        Map<String, String> response = new HashMap<>();
        response.put("username", username);
        return ResponseEntity.ok(response);
    }

}
