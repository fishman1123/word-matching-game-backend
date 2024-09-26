package com.wordsystem.newworldbridge.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class CustomChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private UserSessionRegistry userSessionRegistry;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String username = accessor.getFirstNativeHeader("username");
            String sessionId = accessor.getSessionId();
            String roomId = accessor.getFirstNativeHeader("roomId"); // Get roomId from headers

            if (username != null && sessionId != null) {
                userSessionRegistry.addUser(sessionId, username, roomId);
            }
        }

        return message;
    }
}
