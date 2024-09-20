package com.wordsystem.newworldbridge.controller;

import com.wordsystem.newworldbridge.dto.UserInformation;
import com.wordsystem.newworldbridge.model.service.LoginService;
import com.wordsystem.newworldbridge.model.service.UserInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
