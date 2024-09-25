package com.wordsystem.newworldbridge.model;

import lombok.*;

import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Message {

    private String senderName;
    private Integer userId;
    private String receiverName;
    private String message;
    private String date;
    private Status status;
    private Collection<String> userList; // Ensure this field is present
}
