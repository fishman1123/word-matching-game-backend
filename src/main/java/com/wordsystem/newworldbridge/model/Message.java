package com.wordsystem.newworldbridge.model;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Message {

    private String senderName;
    private Integer userId;
    private String receiverName;
//    private String isReady;
    private String message;
    private String date;
    private Status status;

}
