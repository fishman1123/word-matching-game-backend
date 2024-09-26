package com.wordsystem.newworldbridge.config;

import com.wordsystem.newworldbridge.config.RoomSessionRegistry;
import com.wordsystem.newworldbridge.config.UserSessionRegistry;
import com.wordsystem.newworldbridge.model.Message;
import com.wordsystem.newworldbridge.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Collection;

@Component
public class WebSocketEventListener implements ApplicationListener<SessionDisconnectEvent> {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private UserSessionRegistry userSessionRegistry;

    @Autowired
    private RoomSessionRegistry roomSessionRegistry;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        handleSessionDisconnected(event);
    }

    private void handleSessionDisconnected(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();

        String username = userSessionRegistry.getUsername(sessionId);
        String roomId = userSessionRegistry.getUserRoom(sessionId);

        if (username != null) {
            // Remove user from registries
            userSessionRegistry.removeUser(sessionId);

            // If the user was in a private room, remove from room registry and broadcast update
            if (roomId != null) {
                roomSessionRegistry.removeUserFromRoom(roomId, username);

                // Send LEAVE message to the private room
                Message leaveMessage = new Message();
                leaveMessage.setStatus(Status.LEAVE);
                leaveMessage.setSenderName(username);
                leaveMessage.setMessage("User " + username + " has left the room.");

                simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", leaveMessage);

                // Broadcast updated user list to the room
                broadcastRoomUserList(roomId);
            }

            // Send LEAVE message to the public chatroom
            Message publicLeaveMessage = new Message();
            publicLeaveMessage.setStatus(Status.LEAVE);
            publicLeaveMessage.setSenderName(username);
            publicLeaveMessage.setMessage("User " + username + " has left the chat.");

            simpMessagingTemplate.convertAndSend("/chatroom/public", publicLeaveMessage);

            // Broadcast updated user list to the public chatroom
            broadcastUserList();
        }
    }

    private void broadcastUserList() {
        Collection<String> usernames = userSessionRegistry.getAllUsernames();

        Message message = new Message();
        message.setStatus(Status.USER_LIST);
        message.setUserList(usernames);

        simpMessagingTemplate.convertAndSend("/chatroom/public", message);
    }

    private void broadcastRoomUserList(String roomId) {
        Collection<String> roomUsernames = roomSessionRegistry.getUsersInRoom(roomId);

        Message userListMessage = new Message();
        userListMessage.setStatus(Status.USER_LIST);
        userListMessage.setUserList(roomUsernames);

        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", userListMessage);
    }
}
