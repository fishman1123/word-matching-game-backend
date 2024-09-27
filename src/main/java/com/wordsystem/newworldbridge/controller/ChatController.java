package com.wordsystem.newworldbridge.controller;

import com.wordsystem.newworldbridge.config.RoomSessionRegistry;
import com.wordsystem.newworldbridge.config.UserSessionRegistry;
import com.wordsystem.newworldbridge.dto.RoomStatusInfo;
import com.wordsystem.newworldbridge.dto.UserInformation;
import com.wordsystem.newworldbridge.model.Message;
import com.wordsystem.newworldbridge.model.Status;
import com.wordsystem.newworldbridge.model.service.RoomStatusInfoService;
import com.wordsystem.newworldbridge.model.service.UserInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private UserInformationService userInformationService;

    @Autowired
    private UserSessionRegistry userSessionRegistry;

    @Autowired
    private RoomSessionRegistry roomSessionRegistry;


    @Autowired
    private RoomStatusInfoService roomStatusInfoService;


    @MessageMapping("/room/{roomId}/message")
    public void receiveRoomMessage(@DestinationVariable String roomId, @Payload Message message) {

        System.out.println("Status: " + message.getStatus());
        System.out.println("Message: " + message.getMessage());

        if (message.getStatus() == Status.JOIN) {
            handleRoomUserJoin(roomId, message.getSenderName(), message.getUserId());
        } else if (message.getStatus() == Status.LEAVE) {
            handleRoomUserLeave(roomId, message.getSenderName(), message.getUserId());
        }

        // Broadcast the message to the room
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", message);
    }

    @MessageMapping("/message")
    public void receivePublicMessage(@Payload Message message) {
        if (message.getStatus() == Status.JOIN) {
            handleUserJoin(message.getUserId());
            broadcastUserList();
        } else if (message.getStatus() == Status.LEAVE) {
            handleUserLeave(message.getSenderName(), message.getUserId());
            broadcastUserList();
        }

        // Broadcast the message to the public chatroom
        simpMessagingTemplate.convertAndSend("/chatroom/public", message);
    }

    private void broadcastUserList() {
        Collection<String> usernames = userSessionRegistry.getAllUsernames();

        // Use a Set to ensure uniqueness
        Set<String> uniqueUsernames = new HashSet<>(usernames);

        Message userListMessage = new Message();
        userListMessage.setStatus(Status.USER_LIST);
        userListMessage.setUserList(uniqueUsernames);

        simpMessagingTemplate.convertAndSend("/chatroom/public", userListMessage);
    }

    private void handleUserJoin(Integer userId) {
        try {
            UserInformation userInfo = userInformationService.getUserInformation(userId);
            if (userInfo != null) {
                userInfo.setIsUserPlaying(1); // Set is_user_playing to 1
                userInformationService.updateUserIsPlaying(userId, 1);
                System.out.println("User " + userId + " is now playing.");
            } else {
                System.out.println("User information not found for userId: " + userId);
            }
        } catch (Exception e) {
            System.out.println("Error handling user join: " + e.getMessage());
        }
    }

    private void handleUserLeave(String username, Integer userId) {
        try {
            // Update is_user_playing to 0
            userInformationService.updateUserIsPlaying(userId, 0);
            System.out.println("User " + username + " has left. Updating isUserPlaying to 0.");

            // Remove user from session registry
            userSessionRegistry.removeUserByUsername(username);

        } catch (Exception e) {
            System.out.println("Error handling user leave: " + e.getMessage());
        }
    }

    private void handleRoomUserJoin(String roomId, String username, Integer userId) {
        try {
            // Add user to the room's session registry
            roomSessionRegistry.addUserToRoom(roomId, username);
            System.out.println("User ID: " + userId);

            // Retrieve RoomStatusInfo
            RoomStatusInfo enteredUserStatus = roomStatusInfoService.getRoomStatusInfoById(Integer.valueOf(roomId));

            if (enteredUserStatus != null) {
                System.out.println("Retrieved RoomStatusInfo with ID: " + enteredUserStatus.getId());

                if (!Objects.equals(enteredUserStatus.getId(), userId)) {
                    // User is not the host (whose userId equals roomId)
                    enteredUserStatus.setEnteredPlayerId(userId);
                    enteredUserStatus.setVisitorIsReady(0); // Reset visitorIsReady
                    System.out.println("Set enteredPlayerId to " + userId + " and reset visitorIsReady to 0");
                } else {
                    // User is the host
                    System.out.println("User is the host; no changes to enteredPlayerId");
                }

                // Ensure other fields are not null
                if (enteredUserStatus.getHostIsReady() == null) {
                    enteredUserStatus.setHostIsReady(0);
                }
                if (enteredUserStatus.getVisitorIsReady() == null) {
                    enteredUserStatus.setVisitorIsReady(0);
                }

                // Update RoomStatusInfo
                roomStatusInfoService.updateRoomStatusInfo(enteredUserStatus);

                System.out.println("Updated RoomStatusInfo for room " + roomId);
            } else {
                System.out.println("RoomStatusInfo not found for roomId: " + roomId);
            }

            // Broadcast updated user list to the room
            broadcastRoomUserList(roomId);

            System.out.println("User " + username + " joined room " + roomId);
        } catch (Exception e) {
            System.out.println("Error handling room user join: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void handleRoomUserLeave(String roomId, String username, Integer userId) {
        try {
            // Remove user from the room's session registry
            roomSessionRegistry.removeUserFromRoom(roomId, username);
            RoomStatusInfo roomStatusInfo = roomStatusInfoService.getRoomStatusInfoById(Integer.valueOf(roomId));
            roomStatusInfo.setEnteredPlayerId(null);
            roomStatusInfo.setVisitorIsReady(0);
            roomStatusInfoService.updateRoomStatusInfo(roomStatusInfo);

            // Broadcast updated user list to the room
            broadcastRoomUserList(roomId);

            System.out.println("User " + username + " left room " + roomId);
        } catch (Exception e) {
            System.out.println("Error handling room user leave: " + e.getMessage());
        }
    }

    private void broadcastRoomUserList(String roomId) {
        Collection<String> roomUsernames = roomSessionRegistry.getUsersInRoom(roomId);

        Message userListMessage = new Message();
        userListMessage.setStatus(Status.USER_LIST);
        userListMessage.setUserList(roomUsernames);

        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", userListMessage);
    }
}
