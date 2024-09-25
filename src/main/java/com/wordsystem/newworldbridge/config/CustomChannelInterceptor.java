package com.wordsystem.newworldbridge.config;

import com.wordsystem.newworldbridge.config.UserConnectEvent;
import com.wordsystem.newworldbridge.config.UserSessionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class CustomChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private UserSessionRegistry userSessionRegistry;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String username = accessor.getFirstNativeHeader("username");
            String sessionId = accessor.getSessionId();

            if (username != null && sessionId != null) {
                userSessionRegistry.addUser(sessionId, username);

                UserConnectEvent event = new UserConnectEvent(this, username);
                applicationEventPublisher.publishEvent(event);
            }
        }

        return message;
    }
}
