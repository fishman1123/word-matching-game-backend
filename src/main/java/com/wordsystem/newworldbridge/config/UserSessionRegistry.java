package com.wordsystem.newworldbridge.config;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;

@Component
public class UserSessionRegistry {
    private ConcurrentHashMap<String, String> userSessions = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> sessionRooms = new ConcurrentHashMap<>();

    public void addUser(String sessionId, String username, String roomId) {
        userSessions.put(sessionId, username);
        if (roomId != null) {
            sessionRooms.put(sessionId, roomId);
        }
    }

    public void removeUser(String sessionId) {
        userSessions.remove(sessionId);
        sessionRooms.remove(sessionId);
    }

    public String getUsername(String sessionId) {
        return userSessions.get(sessionId);
    }

    public String getUserRoom(String sessionId) {
        return sessionRooms.get(sessionId);
    }

    public Collection<String> getAllUsernames() {
        return userSessions.values();
    }

    public void removeUserByUsername(String username) {
        userSessions.entrySet().removeIf(entry -> entry.getValue().equals(username));
        sessionRooms.entrySet().removeIf(entry -> userSessions.get(entry.getKey()).equals(username));
    }
}
