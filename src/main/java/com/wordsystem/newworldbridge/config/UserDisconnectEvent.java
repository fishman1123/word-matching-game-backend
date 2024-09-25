package com.wordsystem.newworldbridge.config;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserDisconnectEvent extends ApplicationEvent {
    private String username;

    public UserDisconnectEvent(Object source, String username) {
        super(source);
        this.username = username;
    }

}
