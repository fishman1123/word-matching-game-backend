package com.wordsystem.newworldbridge.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/auth/user-info")
    public Map<String, Object> user(
            @AuthenticationPrincipal OAuth2User principal,
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) {

        Map<String, Object> attributes = new HashMap<>(principal.getAttributes());
        attributes.put("accessToken", authorizedClient.getAccessToken().getTokenValue());
        attributes.put("refreshToken", authorizedClient.getRefreshToken() != null
                ? authorizedClient.getRefreshToken().getTokenValue()
                : null);
        return attributes;
    }
}
