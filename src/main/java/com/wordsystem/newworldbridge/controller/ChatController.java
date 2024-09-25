package com.wordsystem.newworldbridge.controller;

import com.wordsystem.newworldbridge.config.UserSessionRegistry;
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

import java.util.Collection;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private RoomStatusInfoService roomStatusInfoService;

    @Autowired
    private UserInformationService userInformationService; // Use UserInformationService

    @Autowired
    private UserSessionRegistry userSessionRegistry;

    @MessageMapping("/room/{roomId}/message")
    public void receiveRoomMessage(@DestinationVariable String roomId, @Payload Message message) {

        System.out.println("this is the status" + message.getStatus());
        System.out.println("this is the message" + message.getMessage());
//        if (message.getMessage().equals("오")) {
//            message.setMessage("오혜령?");
//        }
        if (message.getStatus() == Status.LEAVE) {
            handleUserLeave(roomId, message.getUserId());
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

//    @MessageMapping("/message")
//    @SendTo("/chatroom/public")
//    private Message recievePublicMessage(@Payload Message message) {
//        return message;
//    }

    @MessageMapping("/message")
    @SendTo("/chatroom/public")
    public Message receivePublicMessage(@Payload Message message) {
        if (message.getStatus() == Status.LEAVE) {
            handleUserLeave(message.getSenderName(), message.getUserId());

            // Broadcast updated user list
            broadcastUserList();
        } else if (message.getStatus() == Status.JOIN) {
            handleUserJoin(message.getUserId());

            // Broadcast updated user list
            broadcastUserList();
        }
        return message;
    }


    private void broadcastUserList() {
        Collection<String> usernames = userSessionRegistry.getAllUsernames();

        Message userListMessage = new Message();
        userListMessage.setStatus(Status.USER_LIST);
        userListMessage.setUserList(usernames);

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
}
