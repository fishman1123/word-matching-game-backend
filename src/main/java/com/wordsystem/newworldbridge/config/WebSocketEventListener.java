package com.wordsystem.newworldbridge.config;

import com.wordsystem.newworldbridge.model.Message;
import com.wordsystem.newworldbridge.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.*;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.util.Collection;

@Component
public class WebSocketEventListener implements ApplicationListener<SessionDisconnectEvent> {

    @Autowired
    private UserSessionRegistry userSessionRegistry;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        handleSessionDisconnected(event);
    }

    private void handleSessionDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        String username = userSessionRegistry.getUsername(sessionId);

        if (username != null) {
            userSessionRegistry.removeUser(sessionId);

            UserDisconnectEvent disconnectEvent = new UserDisconnectEvent(this, username);
            applicationEventPublisher.publishEvent(disconnectEvent);
        }
    }
}
