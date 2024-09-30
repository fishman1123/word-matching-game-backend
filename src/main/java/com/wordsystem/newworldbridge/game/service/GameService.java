// src/main/java/com/wordsystem/newworldbridge/game/service/GameService.java

package com.wordsystem.newworldbridge.game.service;

import com.wordsystem.newworldbridge.dto.RoomStatusInfo;
import com.wordsystem.newworldbridge.model.Message;
import com.wordsystem.newworldbridge.model.Status;
import com.wordsystem.newworldbridge.model.service.RoomStatusInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    // Memory-based map to manage the turn
    private ConcurrentHashMap<String, Integer> currentTurnMap = new ConcurrentHashMap<>();

    // Memory-based map to manage the previous word for each room
    private ConcurrentHashMap<String, String> previousWordsMap = new ConcurrentHashMap<>();

    // Memory-based map to store used words for each room
    private ConcurrentHashMap<String, Set<String>> usedWordsMap = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private RoomStatusInfoService roomStatusInfoService;

    /**
     * Method to send the initial word to the room.
     *
     * @param hostId The ID of the room host.
     */
    public void sendInitialWordToRoom(int hostId) {
        String roomId = String.valueOf(hostId);
        RoomStatusInfo roomStatusInfo = roomStatusInfoService.getRoomStatusInfoById(hostId);
        Integer visitorId = roomStatusInfo.getEnteredPlayerId();

        if (visitorId == null) {
            System.out.println("Visitor ID not found for room: " + roomId);
            return;
        }

        // Send the initial word "박물관" to both users
        Message initialWordMessage = new Message();
        initialWordMessage.setStatus(Status.GAME_IS_ON);
        initialWordMessage.setMessage("word : 박물관");
        initialWordMessage.setSenderName("System");
        initialWordMessage.setUserId(visitorId); // Set the starting player's userId

        // Broadcast to the room
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", initialWordMessage);

        // Initialize the previous word and used words
        previousWordsMap.put(roomId, "박물관");
        usedWordsMap.put(roomId, ConcurrentHashMap.newKeySet());
        usedWordsMap.get(roomId).add("박물관".toLowerCase()); // Store in lowercase for case-insensitive comparison

        // Set the initial turn to the starting player
        currentTurnMap.put(roomId, visitorId);
    }

    /**
     * Retrieves the current turn's user ID for a given room.
     *
     * @param roomId The ID of the room.
     * @return The user ID whose turn it is, or null if not set.
     */
    public Integer getCurrentTurn(String roomId) {
        return currentTurnMap.get(roomId);
    }

    /**
     * Sets the current turn's user ID for a given room.
     *
     * @param roomId The ID of the room.
     * @param userId The user ID to set as the current turn.
     */
    public void setCurrentTurn(String roomId, Integer userId) {
        currentTurnMap.put(roomId, userId);

        // Notify users about the turn change
        sendTurnChangeMessage(roomId);
    }

    /**
     * Removes the current turn's user ID for a given room.
     *
     * @param roomId The ID of the room.
     */
    public void removeCurrentTurn(String roomId) {
        currentTurnMap.remove(roomId);
    }

    /**
     * Changes the turn to the next user in the room.
     *
     * @param roomId    The ID of the room.
     * @param hostId    The user ID of the host.
     * @param visitorId The user ID of the visitor.
     */
    public void changeTurn(String roomId, Integer hostId, Integer visitorId) {
        Integer currentTurnUserId = getCurrentTurn(roomId);

        if (visitorId == null) {
            // Visitor not present, cannot change turn
            return;
        }

        // Determine the next turn: if current turn is host, switch to visitor; if visitor, switch to host
        Integer nextTurnUserId = (currentTurnUserId != null && currentTurnUserId.equals(hostId)) ? visitorId : hostId;

        // Set the next turn
        setCurrentTurn(roomId, nextTurnUserId);
    }

    /**
     * Broadcasts a turn change message to the room.
     *
     * @param roomId The ID of the room.
     */
    public void sendTurnChangeMessage(String roomId) {
        Integer currentTurnUserId = getCurrentTurn(roomId);
        if (currentTurnUserId == null) {
            return; // No current turn set
        }
        Message turnChangeMessage = new Message();
        turnChangeMessage.setStatus(Status.TURN_CHANGE);
        turnChangeMessage.setUserId(currentTurnUserId);
        turnChangeMessage.setMessage("It's now your turn.");
        turnChangeMessage.setSenderName("System");
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", turnChangeMessage);
    }

    public String getPreviousWord(String roomId) {
        return previousWordsMap.getOrDefault(roomId, null);
    }


    public void setPreviousWord(String roomId, String word) {
        previousWordsMap.put(roomId, word);
    }


    public void removePreviousWord(String roomId) {
        previousWordsMap.remove(roomId);
    }


    public boolean isWordDuplicated(String roomId, String word) {
        if (roomId == null || word == null) {
            return false;
        }
        // Initialize the set if it doesn't exist
        usedWordsMap.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet());
        // Attempt to add the word. If it already exists, add() returns false
        return !usedWordsMap.get(roomId).add(word.toLowerCase()); // Case-insensitive
    }


    public void addUsedWord(String roomId, String word) {
        if (roomId == null || word == null) {
            return;
        }
        usedWordsMap.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet());
        usedWordsMap.get(roomId).add(word.toLowerCase()); // Case-insensitive
    }

    /**
     * Clears the used words list for the room.
     *
     * @param roomId The ID of the room.
     */
    public void clearUsedWords(String roomId) {
        Set<String> usedWords = usedWordsMap.get(roomId);
        if (usedWords != null) {
            usedWords.clear();
        }
    }
}
