// src/main/java/com/wordsystem/newworldbridge/controller/ChatController.java

package com.wordsystem.newworldbridge.controller;

import com.wordsystem.newworldbridge.config.RoomSessionRegistry;
import com.wordsystem.newworldbridge.config.UserSessionRegistry;
import com.wordsystem.newworldbridge.dto.RoomStatusInfo;
import com.wordsystem.newworldbridge.dto.UserInformation;
import com.wordsystem.newworldbridge.game.service.GameService;
import com.wordsystem.newworldbridge.model.Message;
import com.wordsystem.newworldbridge.model.Status;
import com.wordsystem.newworldbridge.model.service.RoomStatusInfoService;
import com.wordsystem.newworldbridge.model.service.UserInformationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.*;

@Slf4j
@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private UserInformationService userInformationService;

    @Autowired
    private UserSessionRegistry userSessionRegistry;

    @Autowired
    private RoomSessionRegistry roomSessionRegistry;

    @Autowired
    private RoomStatusInfoService roomStatusInfoService;

    @Autowired
    private GameService gameService; // Inject GameService

    @MessageMapping("/room/{roomId}/message")
    public void receiveRoomMessage(@DestinationVariable String roomId, @Payload Message message) {

        System.out.println("Status: " + message.getStatus());
        System.out.println("Message: " + message.getMessage());

        if (message.getStatus() == Status.JOIN) {
            handleRoomUserJoin(roomId, message.getSenderName(), message.getUserId());
        } else if (message.getStatus() == Status.LEAVE) {
            handleRoomUserLeave(roomId, message.getSenderName(), message.getUserId());
        } else if (message.getStatus() == Status.GAME_IS_ON) {
            // Get the current turn user ID
            Integer currentTurnUserId = gameService.getCurrentTurn(roomId);

            // Check if it's the sender's turn
            if (currentTurnUserId == null || !message.getUserId().equals(currentTurnUserId)) {
                // It's not the sender's turn, send an error message
                Message errorMessage = new Message();
                errorMessage.setStatus(Status.ERROR);
                errorMessage.setMessage("It's not your turn!");
                errorMessage.setSenderName("System");
                errorMessage.setUserId(message.getUserId());
                simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", errorMessage);
                return;
            }

            // 게임로직

            // 이전단어 불러오기
            String previousWord = gameService.getPreviousWord(roomId);

            // 널값일경우 첫번째 단어임
            if (previousWord == null) {
                System.out.println("Previous word is null, using initial word.");
                // 해당 서비스를 통해 이전단어 추출
                previousWord = gameService.getInitialWord(roomId);
            }

            String currentWord = message.getMessage();

            // 단어체크
            if (currentWord != null && currentWord.length() > 0 && previousWord.length() > 0) {
                char lastCharOfPreviousWord = previousWord.charAt(previousWord.length() - 1);
                char firstCharOfCurrentWord = currentWord.charAt(0);

                if (lastCharOfPreviousWord != firstCharOfCurrentWord) {
                    // 단어 틀릴겨우
                    sendGameStatus(roomId, message.getUserId());
                } else {
                    // 중복단어확인
                    if (gameService.isWordDuplicated(roomId, currentWord)) {
                        // 증복단어트리거
                        sendDuplicatedMessage(roomId);
                        // 중복단어로 인한 게임종료트리거
                        sendGameStatus(roomId, message.getUserId());
                        // 어레이 정리
                        gameService.clearUsedWords(roomId);
                        return;
                    }

                    // Correct and non-duplicated word, update the previous word in GameService
                    gameService.setPreviousWord(roomId, currentWord);
                    // Add the current word to used words
                    gameService.addUsedWord(roomId, currentWord);

                    // Broadcast the message to the room
                    simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", message);

                    // Change the turn
                    RoomStatusInfo roomStatusInfo = roomStatusInfoService.getRoomStatusInfoById(Integer.parseInt(roomId));
                    Integer hostId = roomStatusInfo.getId();
                    Integer visitorId = roomStatusInfo.getEnteredPlayerId();

                    gameService.changeTurn(roomId, hostId, visitorId);
                }
            } else {
                // Invalid word
                sendGameStatus(roomId, message.getUserId());
            }
        } else if (message.getStatus() == Status.GAME_IS_OFF) {
            if ("TIMEOUT".equals(message.getMessage())) {
                // Assuming that the message.getUserId() is the player who has timed out
                Integer loserId = message.getUserId();

                // You need to fetch the other player in the room
                RoomStatusInfo roomStatusInfo = roomStatusInfoService.getRoomStatusInfoById(Integer.parseInt(roomId));
                Integer hostId = roomStatusInfo.getId();
                Integer visitorId = roomStatusInfo.getEnteredPlayerId();

                // Determine the winner: if loser is host, winner is visitor, and vice versa
                Integer winnerId = loserId.equals(hostId) ? visitorId : hostId;

                // Create and send the loser message
                Message loserMessage = new Message();
                loserMessage.setStatus(Status.GAME_IS_OFF);
                loserMessage.setSenderName("System");
                loserMessage.setUserId(loserId);
                loserMessage.setMessage("You lose");

                // Create and send the winner message
                Message winnerMessage = new Message();
                winnerMessage.setStatus(Status.GAME_IS_OFF);
                winnerMessage.setSenderName("System");
                winnerMessage.setUserId(winnerId);
                winnerMessage.setMessage("You won");

                // Broadcast the winner and loser messages
                simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", loserMessage);
                simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", winnerMessage);
            } else {
                // Handle other GAME_IS_OFF messages
                simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", message);
            }

        } else {
            // Broadcast the message to the room
            simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", message);
        }
    }




    private void sendDuplicatedMessage(String roomId) {
        Message duplicatedMessage = new Message();
        duplicatedMessage.setStatus(Status.DUPLICATED);
        duplicatedMessage.setMessage("duplicated");
        duplicatedMessage.setSenderName("System");
        duplicatedMessage.setUserId(null); // System message doesn't belong to a specific user
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", duplicatedMessage);
    }

    private void sendGameStatus(String roomId, Integer incorrectUserId) {
        // roomId is host's userId

        Integer hostId = Integer.parseInt(roomId);
        Integer visitorId = null;

        // Get the visitorId from RoomStatusInfo
        RoomStatusInfo roomStatusInfo = roomStatusInfoService.getRoomStatusInfoById(hostId);
        if (roomStatusInfo != null) {
            visitorId = roomStatusInfo.getEnteredPlayerId();
        }

        if (visitorId == null) {
            // No visitor, cannot send game status
            return;
        }

        // Determine winner and loser
        Integer winnerId = incorrectUserId.equals(hostId) ? visitorId : hostId;
        Integer loserId = incorrectUserId;

        // Create separate Message objects for winner and loser
        Message loserMessage = new Message();
        loserMessage.setStatus(Status.GAME_IS_OFF);
        loserMessage.setSenderName("System");
        loserMessage.setUserId(loserId);
        loserMessage.setMessage("You lose");

        Message winnerMessage = new Message();
        winnerMessage.setStatus(Status.GAME_IS_OFF);
        winnerMessage.setSenderName("System");
        winnerMessage.setUserId(winnerId);
        winnerMessage.setMessage("You won");

        // Send the messages to the public topic
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", loserMessage);
        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", winnerMessage);

        // Reset the game state
        gameService.removePreviousWord(roomId); // Remove previous word from GameService
        gameService.removeCurrentTurn(roomId); // Remove turn from GameService
        gameService.clearUsedWords(roomId); // Clear used words
    }

    private void handleRoomUserJoin(String roomId, String username, Integer userId) {
        List<String> initialWords = Arrays.asList("박물관", "자동차", "강아지", "컴퓨터", "음악", "책상", "학교", "바다", "산", "도서관");
        Random random = new Random();
        String randomWord = initialWords.get(random.nextInt(initialWords.size()));

        gameService.setInitialWord(roomId, randomWord);
        try {
            // Add user to the room's session registry
            roomSessionRegistry.addUserToRoom(roomId, username);
            System.out.println("User ID: " + userId);

            // Retrieve RoomStatusInfo
            RoomStatusInfo enteredUserStatus = roomStatusInfoService.getRoomStatusInfoById(Integer.valueOf(roomId));

            if (enteredUserStatus != null) {
                System.out.println("Retrieved RoomStatusInfo with ID: " + enteredUserStatus.getId());

                if (!enteredUserStatus.getId().equals(userId)) {
                    // User is not the host (whose userId equals roomId)
                    enteredUserStatus.setEnteredPlayerId(userId);
                    enteredUserStatus.setVisitorIsReady(0); // Reset visitorIsReady
                    System.out.println("Set enteredPlayerId to " + userId + " and reset visitorIsReady to 0");
                } else {
                    // User is the host
                    System.out.println("User is the host; no changes to enteredPlayerId");
                }

                // Ensure other fields are not null
                if (enteredUserStatus.getHostIsReady() == null) {
                    enteredUserStatus.setHostIsReady(0);
                }
                if (enteredUserStatus.getVisitorIsReady() == null) {
                    enteredUserStatus.setVisitorIsReady(0);
                }

                // Update RoomStatusInfo
                roomStatusInfoService.updateRoomStatusInfo(enteredUserStatus);

                System.out.println("Updated RoomStatusInfo for room " + roomId);
            } else {
                System.out.println("RoomStatusInfo not found for roomId: " + roomId);
            }

            // Broadcast updated user list to the room
            broadcastRoomUserList(roomId);

            System.out.println("User " + username + " joined room " + roomId);
        } catch (Exception e) {
            System.out.println("Error handling room user join: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleRoomUserLeave(String roomId, String username, Integer userId) {
        try {
            // Remove user from the room's session registry
            roomSessionRegistry.removeUserFromRoom(roomId, username);
            RoomStatusInfo roomStatusInfo = roomStatusInfoService.getRoomStatusInfoById(Integer.valueOf(roomId));
            roomStatusInfo.setEnteredPlayerId(null);
            roomStatusInfo.setVisitorIsReady(0);
            roomStatusInfo.setHostIsReady(0); // Reset host's ready status
            roomStatusInfoService.updateRoomStatusInfo(roomStatusInfo);

            // Reset the game state when a user leaves
            gameService.removePreviousWord(roomId); // Remove previous word from GameService
            gameService.removeCurrentTurn(roomId); // Remove turn from GameService
            gameService.clearUsedWords(roomId); // Clear used words

            // Broadcast updated user list to the room
            broadcastRoomUserList(roomId);

            System.out.println("User " + username + " left room " + roomId);
        } catch (Exception e) {
            System.out.println("Error handling room user leave: " + e.getMessage());
        }
    }

    private void broadcastRoomUserList(String roomId) {
        Collection<String> roomUsernames = roomSessionRegistry.getUsersInRoom(roomId);

        Message userListMessage = new Message();
        userListMessage.setStatus(Status.USER_LIST);
        userListMessage.setUserList(roomUsernames);

        simpMessagingTemplate.convertAndSend("/room/" + roomId + "/public", userListMessage);
    }

    @MessageMapping("/message")
    public void receivePublicMessage(@Payload Message message) {
        if (message.getStatus() == Status.JOIN) {
            handleUserJoin(message.getUserId());
            broadcastUserList();
        } else if (message.getStatus() == Status.LEAVE) {
            handleUserLeave(message.getSenderName(), message.getUserId());
            broadcastUserList();
        }

        // Broadcast the message to the public chatroom
        simpMessagingTemplate.convertAndSend("/chatroom/public", message);
    }

    private void broadcastUserList() {
        Collection<String> usernames = userSessionRegistry.getAllUsernames();

        // Use a Set to ensure uniqueness
        Set<String> uniqueUsernames = new HashSet<>(usernames);

        Message userListMessage = new Message();
        userListMessage.setStatus(Status.USER_LIST);
        userListMessage.setUserList(uniqueUsernames);

        simpMessagingTemplate.convertAndSend("/chatroom/public", userListMessage);
    }

    private void handleUserJoin(Integer userId) {
        try {
            UserInformation userInfo = userInformationService.getUserInformation(userId);
            if (userInfo != null) {
                userInfo.setIsUserPlaying(1); // Set is_user_playing to 1
                userInformationService.updateUserIsPlaying(userId, 1);
                System.out.println("User " + userId + " is now playing.");
            } else {
                System.out.println("User information not found for userId: " + userId);
            }
        } catch (Exception e) {
            System.out.println("Error handling user join: " + e.getMessage());
        }
    }

    private void handleUserLeave(String username, Integer userId) {
        try {
            // Update is_user_playing to 0
            userInformationService.updateUserIsPlaying(userId, 0);
            System.out.println("User " + username + " has left. Updating isUserPlaying to 0.");

            // Remove user from session registry
            userSessionRegistry.removeUserByUsername(username);

        } catch (Exception e) {
            System.out.println("Error handling user leave: " + e.getMessage());
        }
    }
}
