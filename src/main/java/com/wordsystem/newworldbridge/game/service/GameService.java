// src/main/java/com/wordsystem/newworldbridge/game/service/GameService.java

package com.wordsystem.newworldbridge.game.service;

import com.wordsystem.newworldbridge.dto.RoomStatusInfo;
import com.wordsystem.newworldbridge.model.Message;
import com.wordsystem.newworldbridge.model.Status;
import com.wordsystem.newworldbridge.model.service.RoomStatusInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GameService {

    // Memory-based map to manage the turn
    private Map<String, Integer> currentTurnMap = new HashMap<>();

    // Memory-based map to manage the previous word for each room
    private Map<String, String> previousWordsMap = new HashMap<>();

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private RoomStatusInfoService roomStatusInfoService;

    // Method to send the initial word to the room
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

        // Broadcast to the room
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", initialWordMessage);

        // Initialize the previous word
        previousWordsMap.put(roomId, "박물관");
    }

    // Method to get the current turn
    public Integer getCurrentTurn(String roomId) {
        return currentTurnMap.get(roomId);
    }

    // Method to set the current turn
    public void setCurrentTurn(String roomId, Integer userId) {
        currentTurnMap.put(roomId, userId);

        // Notify users about the turn change
        sendTurnChangeMessage(roomId);
    }

    // Method to remove the current turn (e.g., when the game ends)
    public void removeCurrentTurn(String roomId) {
        currentTurnMap.remove(roomId);
    }

    // Logic to change turns
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

    // Method to broadcast the turn change message
    public void sendTurnChangeMessage(String roomId) {
        Integer currentTurnUserId = getCurrentTurn(roomId);
        if (currentTurnUserId == null) {
            return; // No current turn set
        }
        Message turnChangeMessage = new Message();
        turnChangeMessage.setStatus(Status.TURN_CHANGE);
        turnChangeMessage.setUserId(currentTurnUserId);
        turnChangeMessage.setMessage("It's now user " + currentTurnUserId + "'s turn.");
        turnChangeMessage.setSenderName("System");
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", turnChangeMessage);
    }

    // Method to get the previous word for a room
    public String getPreviousWord(String roomId) {
        return previousWordsMap.getOrDefault(roomId, null);
    }

    // Method to update the previous word for a room
    public void setPreviousWord(String roomId, String word) {
        previousWordsMap.put(roomId, word);
    }

    // Method to remove the previous word (e.g., when the game ends)
    public void removePreviousWord(String roomId) {
        previousWordsMap.remove(roomId);
    }
}
