package com.wordsystem.newworldbridge.controller;

import com.wordsystem.newworldbridge.dto.Login;
import com.wordsystem.newworldbridge.dto.UserInformation;
import com.wordsystem.newworldbridge.model.service.LoginService;
import com.wordsystem.newworldbridge.model.service.UserInformationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.DefaultRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {


    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final OAuth2AuthorizedClientService authorizedClientService;


    public TestController(OAuth2AuthorizedClientManager authorizedClientManager,
                          OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientManager = authorizedClientManager;
        this.authorizedClientService = authorizedClientService;
    }
    @Autowired
    private LoginService loginService;
    @Autowired
    private UserInformationService userInformationService;

    @GetMapping("/auth/user-info")
    public ResponseEntity<Map<String, Object>> user(
            @AuthenticationPrincipal OAuth2User principal,
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) {

        // Extract attributes from the OAuth2User
        Map<String, Object> attributes = new HashMap<>(principal.getAttributes());

        // Extract access and refresh tokens
        attributes.put("accessToken", authorizedClient.getAccessToken().getTokenValue());
        attributes.put("refreshToken", authorizedClient.getRefreshToken() != null
                ? authorizedClient.getRefreshToken().getTokenValue()
                : null);

        // Extract email and username from the principal's attributes
        System.out.printf("user info: %s\n", attributes);
        String email = (String) principal.getAttribute("email");
        String username = (String) principal.getAttribute("name");

        // Check if the user already exists in the login table by email
        Integer existingUserId = loginService.getIdByEmail(email);

        if (existingUserId == null) {  // If no user is found, save the new user
            // Create a new Login object and set its properties
            Login login = new Login();
            login.setSocialEmail(email);
            loginService.setUser(login);  // Save the user to the login table

            // Get the newly created user's ID (assuming MyBatis returns it after insertion)
            Integer newUserId = loginService.getIdByEmail(email);

            // Create and save UserInformation for the new user
            UserInformation userInfo = new UserInformation();
            userInfo.setId(newUserId);  // Set the user's ID
            userInfo.setUserWin(0);     // Initialize win, lose, room, and playing status
            userInfo.setUserLose(0);
            userInfo.setUserHasRoom(0);
            userInfo.setIsUserPlaying(0);
            userInformationService.setUserInformation(userInfo);  // Save user information to user_information table

            System.out.println("New user and user information saved to the database.");

            // Return a response indicating the user does not exist before
            attributes.put("status", "noexist");
            return ResponseEntity.ok(attributes);
        } else {
            System.out.println("User with email " + email + " already exists.");

            // Check if the user information already exists in the user_information table
            UserInformation existingUserInfo = userInformationService.getUserInformation(existingUserId);
            if (existingUserInfo == null) {
                // Create and save UserInformation if it doesn't exist
                UserInformation userInfo = new UserInformation();
                userInfo.setId(existingUserId);  // Set the user's ID
                userInfo.setUserWin(0);          // Initialize win, lose, room, and playing status
                userInfo.setUserLose(0);
                userInfo.setUserHasRoom(0);
                userInfo.setIsUserPlaying(0);
                userInformationService.setUserInformation(userInfo);  // Save to user_information table

                System.out.println("User information saved for existing user.");
            }

            // Return a response indicating the user already exists
            attributes.put("status", "exist");
            return ResponseEntity.ok(attributes);
        }
    }




    @GetMapping("/auth/check-token")
    public ResponseEntity<Void> checkToken(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("Authentication is null or not authenticated.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        // Extract client registration ID (e.g., 'google' or 'github')
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        String principalName = authentication.getName();

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                registrationId, principalName);

        if (authorizedClient == null) {
            System.out.println("Authorized client not found.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();

        if (accessToken == null || accessToken.getExpiresAt() == null) {
            System.out.println("Access token or expiration time is null.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Instant now = Instant.now();

        if (accessToken.getExpiresAt().isBefore(now)) {
            System.out.println("Access token is expired.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        System.out.println("Access token is valid.");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/set-nickname")
    public ResponseEntity<String> setNickname(@RequestBody Map<String, String> requestBody,
                                              @AuthenticationPrincipal OAuth2User principal) {
        String nickname = requestBody.get("nickname");
        System.out.printf(nickname);
        if (nickname.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Nickname is required");
        }

        // Get the user's email from the authentication principal
        String email = principal.getAttribute("email");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User email not found");
        }

        // Update the username in the database
        try {
            loginService.updateUsernameByEmail(email, nickname);
            return ResponseEntity.ok("Nickname updated successfully");
        } catch (Exception e) {
            // Handle exceptions, e.g., user not found
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update nickname");
        }
    }

    @PostMapping("/auth/refresh-token")
    public Map<String, Object> refreshAccessToken(@RequestBody Map<String, String> requestBody) {
        String refreshTokenValue = requestBody.get("refreshToken");

        if (refreshTokenValue == null) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getName();
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                registrationId, principalName);

        if (authorizedClient == null) {
            throw new IllegalStateException("Authorized client not found");
        }

        if (!refreshTokenValue.equals(authorizedClient.getRefreshToken().getTokenValue())) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // Create a refresh token grant request
        OAuth2RefreshTokenGrantRequest refreshTokenGrantRequest = new OAuth2RefreshTokenGrantRequest(
                authorizedClient.getClientRegistration(),
                authorizedClient.getAccessToken(),
                authorizedClient.getRefreshToken(),
                null);

        // Use a default refresh token response client
        OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> accessTokenResponseClient =
                new DefaultRefreshTokenTokenResponseClient();

        // Get a new access token
        OAuth2AccessTokenResponse tokenResponse = accessTokenResponseClient.getTokenResponse(refreshTokenGrantRequest);

        // Update the authorized client
        OAuth2AuthorizedClient updatedAuthorizedClient = new OAuth2AuthorizedClient(
                authorizedClient.getClientRegistration(),
                principalName,
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken());

        // Save the updated authorized client
        authorizedClientService.saveAuthorizedClient(updatedAuthorizedClient, authentication);

        // Add System.out.println statements to check new tokens and expiration
        System.out.println("New Access Token: " + tokenResponse.getAccessToken().getTokenValue());
        System.out.println("New Access Token Expires At: " + tokenResponse.getAccessToken().getExpiresAt());

        if (tokenResponse.getRefreshToken() != null) {
            System.out.println("New Refresh Token: " + tokenResponse.getRefreshToken().getTokenValue());
            // Note: Refresh token expiration is often not provided
        } else {
            System.out.println("Refresh Token did not change.");
        }

        // Return the new access token
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", tokenResponse.getAccessToken().getTokenValue());
        return response;
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // Remove the authorized client from the authorized client service
            String principalName = authentication.getName();
            String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

            authorizedClientService.removeAuthorizedClient(registrationId, principalName);

            // Invalidate the session
            request.getSession().invalidate();

            // Clear the security context
            SecurityContextHolder.clearContext();

            // 로그아웃 로그
            System.out.println("User logged out successfully.");
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
