// UserSessionRegistry.java
package com.wordsystem.newworldbridge.config;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserSessionRegistry {
    private ConcurrentHashMap<String, String> userSessions = new ConcurrentHashMap<>(); // sessionId -> username
    private ConcurrentHashMap<String, String> sessionRooms = new ConcurrentHashMap<>(); // sessionId -> roomId

    // Add user only if username is not already present
    public void addUser(String sessionId, String username, String roomId) {
//        if (!userSessions.containsValue(username)) {
            userSessions.put(sessionId, username);
            if (roomId != null) {
                sessionRooms.put(sessionId, roomId);
            }
            System.out.println("Added user: Username = " + username + ", Session ID = " + sessionId + ", Room ID = " + roomId);
//        } else {
//            System.out.println("Username " + username + " already exists. Not adding to registry.");
//            System.out.println("Associated Room ID = " + roomId + " with Session ID = " + sessionId);
//        }
    }

    public void removeUser(String sessionId) {
        String username = userSessions.get(sessionId);
        String roomId = sessionRooms.get(sessionId);

        userSessions.remove(sessionId);
        sessionRooms.remove(sessionId);

        System.out.println("Removed user: Username = " + username + ", Session ID = " + sessionId + ", Room ID = " + roomId);
    }

    public String getUsername(String sessionId) {
        String username = userSessions.get(sessionId);
        System.out.println("getUsername called: Session ID = " + sessionId + ", Username = " + username);
        return username;
    }

    public String getUserRoom(String sessionId) {
        String roomId = sessionRooms.get(sessionId);
        System.out.println("getUserRoom called: Session ID = " + sessionId + ", Room ID = " + roomId);
        return roomId;
    }

    public Collection<String> getAllUsernames() {
        // Return unique usernames
        Collection<String> usernames = userSessions.values().stream().distinct().collect(Collectors.toList());
        System.out.println("getAllUsernames called: Usernames = " + usernames);
        return usernames;
    }

    public void removeUserByUsername(String username) {
        // Remove all sessions associated with the username
        for (Map.Entry<String, String> entry : userSessions.entrySet()) {
            if (entry.getValue().equals(username)) {
                String sessionId = entry.getKey();
                String roomId = sessionRooms.get(sessionId);

                userSessions.remove(sessionId);
                sessionRooms.remove(sessionId);

                System.out.println("Removed user by username: Username = " + username + ", Session ID = " + sessionId + ", Room ID = " + roomId);
            }
        }
    }
}
