package com.wordsystem.newworldbridge.config;

import org.springframework.context.ApplicationEvent;

public class UserConnectEvent extends ApplicationEvent {
    private String username;

    public UserConnectEvent(Object source, String username) {
        super(source);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
