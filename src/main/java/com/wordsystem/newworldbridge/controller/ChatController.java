package com.wordsystem.newworldbridge.controller;


import com.wordsystem.newworldbridge.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate simpleMessagingTemplate;

    @MessageMapping("/message")
    @SendTo("/chatroom/public")
    private Message recievePublicMessage(@Payload Message message) {
        return message;
    }

    @MessageMapping("/private-message")
    public Message receivePrivateMessage(@Payload Message message) {

        simpleMessagingTemplate.convertAndSendToUser(message.getReceiverName(), "/private", message);
        return message;
    }
}
