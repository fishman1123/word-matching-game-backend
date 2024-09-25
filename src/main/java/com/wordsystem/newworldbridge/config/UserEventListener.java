package com.wordsystem.newworldbridge.config;

import com.wordsystem.newworldbridge.model.Message;
import com.wordsystem.newworldbridge.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Collection;

@Component
public class UserEventListener implements ApplicationListener<SessionDisconnectEvent> {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private UserSessionRegistry userSessionRegistry;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        handleSessionDisconnected(event);
    }

    private void handleSessionDisconnected(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();

        String username = userSessionRegistry.getUsername(sessionId);

        if (username != null) {
            userSessionRegistry.removeUser(sessionId);

            // Broadcast updated user list
            broadcastUserList();

            // Send LEAVE message
            Message leaveMessage = new Message();
            leaveMessage.setStatus(Status.LEAVE);
            leaveMessage.setSenderName(username);
            simpMessagingTemplate.convertAndSend("/chatroom/public", leaveMessage);
        }
    }

    private void broadcastUserList() {
        Collection<String> usernames = userSessionRegistry.getAllUsernames();

        Message message = new Message();
        message.setStatus(Status.USER_LIST);
        message.setUserList(usernames);

        simpMessagingTemplate.convertAndSend("/chatroom/public", message);
    }
}