package com.wordsystem.newworldbridge.controller;

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

    @GetMapping("/auth/user-info")
    public Map<String, Object> user(
            @AuthenticationPrincipal OAuth2User principal,
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) {

        Map<String, Object> attributes = new HashMap<>(principal.getAttributes());
        attributes.put("accessToken", authorizedClient.getAccessToken().getTokenValue());
        attributes.put("refreshToken", authorizedClient.getRefreshToken() != null
                ? authorizedClient.getRefreshToken().getTokenValue()
                : null);

        // Add System.out.println statements to check tokens and expiration
        System.out.println("Access Token: " + authorizedClient.getAccessToken().getTokenValue());
        System.out.println("Access Token Expires At: " + authorizedClient.getAccessToken().getExpiresAt());

        if (authorizedClient.getRefreshToken() != null) {
            System.out.println("Refresh Token: " + authorizedClient.getRefreshToken().getTokenValue());
            System.out.println("Refresh Token Expires At: " + authorizedClient.getRefreshToken().getExpiresAt());
        } else {
            System.out.println("No Refresh Token available.");
        }

        return attributes;
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
}
