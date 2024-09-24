package com.wordsystem.newworldbridge.controller;

import com.wordsystem.newworldbridge.model.Message;
import com.wordsystem.newworldbridge.model.Status;
import com.wordsystem.newworldbridge.model.service.RoomStatusInfoService;
import com.wordsystem.newworldbridge.model.service.UserInformationService;
import com.wordsystem.newworldbridge.dto.RoomStatusInfo;
import com.wordsystem.newworldbridge.dto.UserInformation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
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

        System.out.println("this is the status" + message.getStatus());
        System.out.println("this is the message" + message.getMessage());
//        if (message.getMessage().equals("오")) {
//            message.setMessage("오혜령?");
//        }
        if (message.getStatus() == Status.LEAVE) {
            handleUserLeave(roomId, message.getSenderName(), message.getUserId());
        } else if (message.getStatus() == Status.JOIN) {
            handleUserJoin(message.getUserId());
        }
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", message);
    }

    @MessageMapping("/private-message")
    public void receivePrivateMessage(@Payload Message message) {
        System.out.println("this is the message" + message.getMessage());
        simpMessagingTemplate.convertAndSendToUser(message.getReceiverName(), "/private", message);
    }

    @MessageMapping("/message")
    @SendTo("/chatroom/public")
    private Message recievePublicMessage(@Payload Message message) {
        return message;
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
