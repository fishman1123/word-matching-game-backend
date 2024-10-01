// src/main/java/com/wordsystem/newworldbridge/game/service/GameService.java

package com.wordsystem.newworldbridge.game.service;

import com.wordsystem.newworldbridge.dto.RoomStatusInfo;
import com.wordsystem.newworldbridge.model.Message;
import com.wordsystem.newworldbridge.model.Status;
import com.wordsystem.newworldbridge.model.service.RoomStatusInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    // Memory-based map to manage the turn
    private ConcurrentHashMap<String, Integer> currentTurnMap = new ConcurrentHashMap<>();

    // Memory-based map to manage the previous word for each room
    private ConcurrentHashMap<String, String> previousWordsMap = new ConcurrentHashMap<>();

    // Memory-based map to store used words for each room
    private ConcurrentHashMap<String, Set<String>> usedWordsMap = new ConcurrentHashMap<>();

    private Map<String, String> initialWordMap = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private RoomStatusInfoService roomStatusInfoService;


    public void sendInitialWordToRoom(int hostId) {
        String roomId = String.valueOf(hostId);
        RoomStatusInfo roomStatusInfo = roomStatusInfoService.getRoomStatusInfoById(hostId);
        Integer visitorId = roomStatusInfo.getEnteredPlayerId();

        if (visitorId == null) {
            System.out.println("Visitor ID not found for room: " + roomId);
            return;
        }

        // Define a list of possible initial words
        List<String> initialWords = Arrays.asList("박물관", "자동차", "강아지", "컴퓨터", "음악", "책상", "학교", "바다", "산", "도서관");

        // Pick a random word from the list
        Random random = new Random();
        String randomWord = initialWords.get(random.nextInt(initialWords.size()));

        // Send the initial word to both users
        Message initialWordMessage = new Message();
        initialWordMessage.setStatus(Status.GAME_IS_ON);
        initialWordMessage.setMessage("word : " + randomWord);
        initialWordMessage.setSenderName("System");
        initialWordMessage.setUserId(visitorId); // Set the starting player's userId

        // Broadcast to the room
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", initialWordMessage);

        // Initialize the previous word and used words
        previousWordsMap.put(roomId, randomWord);
        usedWordsMap.put(roomId, ConcurrentHashMap.newKeySet());
        usedWordsMap.get(roomId).add(randomWord.toLowerCase()); // Store in lowercase for case-insensitive comparison

        // Set the initial turn to the starting player
        currentTurnMap.put(roomId, visitorId);
    }


    public Integer getCurrentTurn(String roomId) {
        return currentTurnMap.get(roomId);
    }


    public void setCurrentTurn(String roomId, Integer userId) {
        currentTurnMap.put(roomId, userId);

        // Notify users about the turn change
        sendTurnChangeMessage(roomId);
    }

    public String getInitialWord(String roomId) {
        return initialWordMap.get(roomId);
    }

    public void setInitialWord(String roomId, String word) {
        initialWordMap.put(roomId, word);
    }



    public void removeCurrentTurn(String roomId) {
        currentTurnMap.remove(roomId);
    }


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


    public void clearUsedWords(String roomId) {
        Set<String> usedWords = usedWordsMap.get(roomId);
        if (usedWords != null) {
            usedWords.clear();
        }
    }
}
