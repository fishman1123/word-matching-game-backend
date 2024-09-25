package com.wordsystem.newworldbridge.config;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;

@Component
public class UserSessionRegistry {
    private ConcurrentHashMap<String, String> userSessions = new ConcurrentHashMap<>();

    public void addUser(String sessionId, String username) {
        userSessions.put(sessionId, username);
    }

    public void removeUser(String sessionId) {
        userSessions.remove(sessionId);
    }

    public String getUsername(String sessionId) {
        return userSessions.get(sessionId);
    }

    public Collection<String> getAllUsernames() {
        return userSessions.values();
    }

    public void removeUserByUsername(String username) {
        userSessions.entrySet().removeIf(entry -> entry.getValue().equals(username));
    }
}
