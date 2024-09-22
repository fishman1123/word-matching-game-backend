package com.wordsystem.newworldbridge.controller;

import com.wordsystem.newworldbridge.model.Message;
import com.wordsystem.newworldbridge.model.service.RoomStatusInfoService;
import com.wordsystem.newworldbridge.model.service.UserInformationService;
import com.wordsystem.newworldbridge.dto.RoomStatusInfo;
import com.wordsystem.newworldbridge.dto.UserInformation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private RoomStatusInfoService roomStatusInfoService;

    @Autowired
    private UserInformationService userInformationService; // Use UserInformationService

    @MessageMapping("/room/{roomId}/message")
    public void receiveRoomMessage(@DestinationVariable String roomId, @Payload Message message) {
        if ("LEAVE".equals(message.getStatus())) {
            handleUserLeave(roomId, message.getSenderName(), message.getUserId());
        } else if ("JOIN".equals(message.getStatus())) {
            handleUserJoin(message.getUserId());
        }
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", message);
    }

    @MessageMapping("/private-message")
    public void receivePrivateMessage(@Payload Message message) {
        simpMessagingTemplate.convertAndSendToUser(message.getReceiverName(), "/private", message);
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

    private void handleUserLeave(String roomId, String username, Integer userId) {
        try {
            // Update is_user_playing to 0
            userInformationService.updateUserIsPlaying(userId, 0);
            System.out.println("User " + username + " has left the room. Updating isUserPlaying to 0.");

            int hostId = Integer.parseInt(roomId);
            RoomStatusInfo roomStatusInfo = roomStatusInfoService.getRoomStatusInfoById(hostId);

            if (roomStatusInfo != null) {
                // Check if the user is the visitor
                if (roomStatusInfo.getEnteredPlayerId() != null && roomStatusInfo.getEnteredPlayerId().equals(userId)) {
                    roomStatusInfo.setEnteredPlayerId(null);
                    roomStatusInfoService.updateRoomStatusInfo(roomStatusInfo);
                    System.out.println("Resetting enteredPlayerId for roomId: " + roomId);
                } else if (roomStatusInfo.getId().equals(userId)) {
                    // If the host leaves
                    System.out.println("Host " + username + " has left the room.");
                    // Optionally handle room cleanup
                }
            } else {
                System.out.println("Room status info not found for roomId: " + roomId);
            }
        } catch (Exception e) {
            System.out.println("Error handling user leave: " + e.getMessage());
        }
    }
}
