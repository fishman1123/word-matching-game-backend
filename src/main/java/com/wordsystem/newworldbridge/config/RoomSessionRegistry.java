package com.wordsystem.newworldbridge.config;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomSessionRegistry {
    private final ConcurrentHashMap<String, Set<String>> roomUserSessions = new ConcurrentHashMap<>();

    public void addUserToRoom(String roomId, String username) {
        roomUserSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(username);
    }

    public void removeUserFromRoom(String roomId, String username) {
        Set<String> users = roomUserSessions.get(roomId);
        if (users != null) {
            users.remove(username);
            if (users.isEmpty()) {
                roomUserSessions.remove(roomId);
            }
        }
    }

    public Collection<String> getUsersInRoom(String roomId) {
        return roomUserSessions.getOrDefault(roomId, Collections.emptySet());
    }
}
