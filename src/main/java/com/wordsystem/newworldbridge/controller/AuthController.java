
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
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {


    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final OAuth2AuthorizedClientService authorizedClientService;


    public AuthController(OAuth2AuthorizedClientManager authorizedClientManager,
                          OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientManager = authorizedClientManager;
        this.authorizedClientService = authorizedClientService;
    }
    @Autowired
    private LoginService loginService;
    @Autowired
    private UserInformationService userInformationService;

    // OAuth2 Callback 핸들러
    // OAuth2 Callback 핸들러
    @GetMapping("/auth/callback")
    public void oauth2Callback(
            @AuthenticationPrincipal OidcUser principal,
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
            HttpServletResponse response) throws IOException {

        // 사용자 정보 가져오기
        Map<String, Object> attributes = new HashMap<>(principal.getAttributes());
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String email = (String) principal.getAttribute("email");

        // 사용자 정보 처리 로직 (DB 저장 또는 조회)
        Integer existingUserId = loginService.getIdByEmail(email);
        if (existingUserId == null) {
            // 새 사용자 생성 로직
            Login newLogin = new Login();
            newLogin.setSocialEmail(email);
            loginService.setUser(newLogin);

            Integer newUserId = loginService.getIdByEmail(email);

            UserInformation newUserInfo = new UserInformation();
            newUserInfo.setId(newUserId);
            newUserInfo.setUserWin(0);
            newUserInfo.setUserLose(0);
            newUserInfo.setUserHasRoom(0);
            newUserInfo.setIsUserPlaying(0);
            userInformationService.setUserInformation(newUserInfo);

            // 상태를 noexist로 설정하여 프론트엔드에 전송
            attributes.put("status", "noexist");
        } else {
            // 사용자가 이미 존재하는 경우 닉네임을 설정했는지 확인
            String targetName = loginService.getUserNameByEmail(email);
            if (targetName == null) {
                // 닉네임이 없으면 상태를 noexist로 설정
                attributes.put("status", "noexist");
            } else {
                // 닉네임이 있으면 상태를 exist로 설정
                attributes.put("status", "exist");
            }
        }

        // JavaScript로 메인 창으로 메시지 전송
        response.setContentType("text/html");
        response.getWriter().write("<script>");
        response.getWriter().write("window.opener.postMessage({ token: '" + accessToken + "', status: '" + attributes.get("status") + "' }, 'http://localhost:5173');");
        response.getWriter().write("window.close();");
        response.getWriter().write("</script>");
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
