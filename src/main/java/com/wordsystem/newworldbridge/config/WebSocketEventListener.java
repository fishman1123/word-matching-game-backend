package com.wordsystem.newworldbridge.config;

import com.wordsystem.newworldbridge.config.RoomSessionRegistry;
import com.wordsystem.newworldbridge.config.UserSessionRegistry;
import com.wordsystem.newworldbridge.dto.RoomInfo;
import com.wordsystem.newworldbridge.dto.RoomStatusInfo;
import com.wordsystem.newworldbridge.model.Message;
import com.wordsystem.newworldbridge.model.Status;
import com.wordsystem.newworldbridge.model.service.LoginService;
import com.wordsystem.newworldbridge.model.service.RoomInfoService;
import com.wordsystem.newworldbridge.model.service.RoomStatusInfoService;
import com.wordsystem.newworldbridge.model.service.UserInformationService;
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

    @Autowired
    private UserInformationService userInformationService;

    @Autowired
    private RoomInfoService roomInfoService;

    @Autowired
    private RoomStatusInfoService roomStatusInfoService;

    @Autowired
    private LoginService loginService; // Use LoginService to get userId

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        System.out.println(event);
        handleSessionDisconnected(event);
    }

    private void handleSessionDisconnected(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();



        String username = userSessionRegistry.getUsername(sessionId);
        String roomId = userSessionRegistry.getUserRoom(sessionId);
        System.out.println("super username: " + username);

        if (username != null) {
            // Remove user from registries
            userSessionRegistry.removeUser(sessionId);

            // Fetch userId using LoginService
            Integer userId = loginService.getId(username);

            System.out.println(userId);
            if (userId == null) {
                System.err.println("User ID not found for username: " + username);
                return;
            }

            if (roomId != null) {
                // If the user was in a private room, remove from room registry and broadcast update
                roomSessionRegistry.removeUserFromRoom(roomId, username);

                // Handle database updates based on user role
                handleUserDisconnection(username, userId, roomId);

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

    private void handleUserDisconnection(String username, Integer userId, String roomId) {
        try {
            int roomIdInt = Integer.parseInt(roomId);
            System.out.println(roomIdInt);

            // Fetch room info and status info
            RoomInfo roomInfo = roomInfoService.getRoom(roomIdInt);
            RoomStatusInfo roomStatusInfo = roomStatusInfoService.getRoomStatusInfoById(roomIdInt);

            if (roomInfo == null || roomStatusInfo == null) {
                // Room does not exist, handle error if necessary
                System.err.println("Room not found for ID: " + roomIdInt);
                return;
            }

            // Check if the game is on
            if (roomInfo.getInGame() == 1) {  // Assuming '1' indicates 'GAME_IS_ON'
                // Send GAME_IS_OFF message to the room
                Message gameOffMessage = new Message();
                gameOffMessage.setStatus(Status.GAME_IS_OFF);
                gameOffMessage.setSenderName("System");
                gameOffMessage.setUserId(999);
                gameOffMessage.setMessage("Game has been terminated due to player disconnection.");

                simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", gameOffMessage);

                // Update the game state in the room info to reflect that the game is off
                roomInfoService.setInGame(roomIdInt, 0);
                System.out.println("Game in room " + roomIdInt + " has been set to OFF due to disconnection.");
            }

            if (roomInfo.getId() == userId) {
                // User is the host
                // Delete room info and status info
                roomInfoService.deleteRoom(roomIdInt);
                roomStatusInfoService.deleteRoomStatusInfoById(roomIdInt);

                // Update user information
                userInformationService.updateUserHasRoom(userId, 0);
                userInformationService.updateUserIsPlaying(userId, 0);

                System.out.println("Host " + username + " disconnected. Room " + roomIdInt + " deleted.");
            } else if (roomStatusInfo.getEnteredPlayerId() != null && roomStatusInfo.getEnteredPlayerId().equals(userId)) {
                // User is the visitor
                // Reset room status fields
                roomStatusInfo.setVisitorIsReady(0);
                roomStatusInfo.setEnteredPlayerId(null);
                roomStatusInfoService.updateRoomStatusInfo(roomStatusInfo);

                // Update user information
                userInformationService.updateUserIsPlaying(userId, 0);

                System.out.println("Visitor " + username + " disconnected from room " + roomIdInt);
            } else {
                // User is not part of the room
                System.err.println("User " + username + " is not part of room " + roomIdInt);
            }
        } catch (Exception e) {
            // Log error
            System.err.println("Error handling user disconnection: " + e.getMessage());
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
