// src/main/java/com/wordsystem/newworldbridge/controller/RoomInfoController.java

package com.wordsystem.newworldbridge.controller;

import com.wordsystem.newworldbridge.dto.RoomInfo;
import com.wordsystem.newworldbridge.dto.RoomStatusInfo;
import com.wordsystem.newworldbridge.game.service.GameService;
import com.wordsystem.newworldbridge.model.Message;
import com.wordsystem.newworldbridge.model.Status;
import com.wordsystem.newworldbridge.model.service.LoginService;
import com.wordsystem.newworldbridge.model.service.RoomInfoService;
import com.wordsystem.newworldbridge.model.service.RoomStatusInfoService;
import com.wordsystem.newworldbridge.model.service.UserInformationService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class RoomInfoController {

    @Autowired
    private RoomInfoService roomInfoService;

    @Autowired
    private UserInformationService userInformationService;

    @Autowired
    private RoomStatusInfoService roomStatusInfoService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private LoginService loginService;

    @Autowired
    private GameService gameService; // Inject GameService

    @PostMapping("/room")
    @Transactional
    public ResponseEntity<Map<String, Object>> createOrUpdateRoom(@RequestBody RoomRequest roomRequest) {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            int userId = roomRequest.getUserId();
            double latitude = roomRequest.getLatitude();
            double longitude = roomRequest.getLongitude();

            // Check if the user already has a room
            RoomInfo existingRoom = roomInfoService.getRoom(userId);

            if (existingRoom != null) {
                // User already has a room, update the room information
                existingRoom.setRoomLocationLatitude(latitude);
                existingRoom.setRoomLocationLongitude(longitude);
                roomInfoService.updateRoom(existingRoom);

                responseMap.put("message", "Room updated successfully");
                responseMap.put("roomId", userId);
                return ResponseEntity.ok(responseMap);
            } else {
                // Create and save new RoomInfo
                RoomInfo roomInfo = new RoomInfo();
                roomInfo.setId(userId);
                roomInfo.setInGame(0); // Initial in-game status
                roomInfo.setRoomLocationLatitude(latitude);
                roomInfo.setRoomLocationLongitude(longitude);

                roomInfoService.setRoom(roomInfo);

                // Update user_information to indicate the user has a room
                userInformationService.updateUserHasRoom(userId, 1);

                // Create and save RoomStatusInfo for the new room
                RoomStatusInfo roomStatusInfo = new RoomStatusInfo();
                roomStatusInfo.setId(userId); // Set the user's ID
                roomStatusInfo.setEnteredPlayerId(null); // Initial value is null
                roomStatusInfo.setHostIsReady(0); // Initialize to 0
                roomStatusInfo.setVisitorIsReady(0); // Initialize to 0

                roomStatusInfoService.insertRoomStatusInfo(roomStatusInfo); // Save to room_status_info table

                responseMap.put("message", "Room created successfully");
                responseMap.put("roomId", userId);
                return ResponseEntity.ok(responseMap);
            }
        } catch (Exception e) {
            // Handle exceptions
            responseMap.put("message", "Error creating/updating room: " + e.getMessage());
            return ResponseEntity.status(500).body(responseMap);
        }
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<Map<String, Object>>> getAllRoomsWithUsernames() {
        try {
            // Fetch all rooms from RoomInfoService
            List<RoomInfo> rooms = roomInfoService.getAllRooms();

            // Create a list to hold the final data
            List<Map<String, Object>> roomsWithUsernames = new ArrayList<>();

            // For each room, fetch the corresponding username by room ID (which is the owner's ID)
            for (RoomInfo room : rooms) {
                String username = loginService.getUserNameById(room.getId());

                // Create a map to hold both the room and the username
                Map<String, Object> roomWithUsername = new HashMap<>();
                roomWithUsername.put("room", room);          // Add room info
                roomWithUsername.put("username", username);  // Add the username

                // Add the map to the final list
                roomsWithUsernames.add(roomWithUsername);
            }

            // Return the list of rooms with usernames
            return ResponseEntity.ok(roomsWithUsernames);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/room/{hostId}/status")
    public ResponseEntity<RoomStatusInfo> getRoomStatus(@PathVariable int hostId) {
        try {
            RoomStatusInfo roomStatusInfo = roomStatusInfoService.getRoomStatusInfoById(hostId);
            if (roomStatusInfo == null) {
                return ResponseEntity.status(404).body(null);
            }
            return ResponseEntity.ok(roomStatusInfo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/room/{roomId}/enter")
    public ResponseEntity<String> updateEnteredPlayerId(
            @PathVariable int roomId,
            @RequestParam("enteredPlayerId") int enteredPlayerId) {
        try {
            RoomStatusInfo roomStatusInfo = roomStatusInfoService.getRoomStatusInfoById(roomId);
            if (roomStatusInfo == null) {
                return ResponseEntity.status(404).body("Room not found");
            }

            if (enteredPlayerId == roomId || enteredPlayerId == 0) {
                roomStatusInfo.setEnteredPlayerId(null);
                roomStatusInfo.setVisitorIsReady(0);
            } else {
                roomStatusInfo.setEnteredPlayerId(enteredPlayerId);
            }

            roomStatusInfoService.updateRoomStatusInfo(roomStatusInfo);
            return ResponseEntity.ok("Entered player ID updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating room status: " + e.getMessage());
        }
    }

    @PutMapping("/room/{hostId}/ready")
    public ResponseEntity<String> updateReadyStatus(
            @PathVariable int hostId,
            @RequestBody Map<String, Object> requestBody) {

        try {
            Object userIdObj = requestBody.get("userId");
            Integer userId = null;

            if (userIdObj instanceof Integer) {
                userId = (Integer) userIdObj;
            } else if (userIdObj instanceof Number) {
                userId = ((Number) userIdObj).intValue();
            } else if (userIdObj instanceof String) {
                userId = Integer.parseInt((String) userIdObj);
            } else {
                return ResponseEntity.status(400).body("Invalid userId");
            }

            Boolean forceReadyStatus = (Boolean) requestBody.get("forceReadyStatus");

            RoomStatusInfo roomStatusInfo = roomStatusInfoService.getRoomStatusInfoById(hostId);
            if (roomStatusInfo == null) {
                return ResponseEntity.status(404).body("Room not found");
            }

            boolean isReady;

            if (roomStatusInfo.getId().equals(userId)) {
                if (forceReadyStatus != null) {
                    isReady = forceReadyStatus;
                    roomStatusInfo.setHostIsReady(forceReadyStatus ? 1 : 0);
                } else {
                    isReady = roomStatusInfo.getHostIsReady() == 1 ? false : true;
                    roomStatusInfo.setHostIsReady(isReady ? 1 : 0);
                }
            } else if (roomStatusInfo.getEnteredPlayerId() != null && roomStatusInfo.getEnteredPlayerId().equals(userId)) {
                if (forceReadyStatus != null) {
                    isReady = forceReadyStatus;
                    roomStatusInfo.setVisitorIsReady(forceReadyStatus ? 1 : 0);
                } else {
                    isReady = roomStatusInfo.getVisitorIsReady() == 1 ? false : true;
                    roomStatusInfo.setVisitorIsReady(isReady ? 1 : 0);
                }
            } else {
                return ResponseEntity.status(400).body("User not part of this room");
            }

            roomStatusInfoService.updateRoomStatusInfo(roomStatusInfo);

            // Send READY or NOT_READY message to clients
            Message statusMessage = new Message();
            statusMessage.setMessage("Ready status updated");
            statusMessage.setUserId(userId);
            statusMessage.setSenderName(loginService.getUserNameById(userId)); // Optional: Include sender's username
            statusMessage.setStatus(isReady ? Status.READY : Status.NOT_READY);
            simpMessagingTemplate.convertAndSend("/room/" + hostId + "/public", statusMessage);

            // Check if both users are ready
            if (roomStatusInfo.getHostIsReady() == 1 && roomStatusInfo.getVisitorIsReady() == 1) {
                roomInfoService.setInGame(hostId, 1);
                // Start the game by sending the initial word using GameService
                gameService.sendInitialWordToRoom(hostId);

                // Set the initial turn to the host or visitor (choose as per your game rules)
                // For example, let's set it to the host
                gameService.setCurrentTurn(String.valueOf(hostId), hostId);
            } else {
                roomInfoService.setInGame(hostId, 0);
            }

            return ResponseEntity.ok("Ready status updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating ready status: " + e.getMessage());
        }
    }

    @GetMapping("/room/{roomId}/verify-location")
    public ResponseEntity<Map<String, Object>> verifyRoomLocation(
            @PathVariable int roomId,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            RoomInfo roomInfo = roomInfoService.getRoom(roomId);
            if (roomInfo == null) {
                responseMap.put("message", "Room not found");
                return ResponseEntity.status(404).body(responseMap);
            }

            if (Double.compare(roomInfo.getRoomLocationLatitude(), latitude) == 0 &&
                    Double.compare(roomInfo.getRoomLocationLongitude(), longitude) == 0) {
                if (roomStatusInfoService.getRoomStatusInfoById(roomId).getEnteredPlayerId() != null) {
                    responseMap.put("message", "Room is occupied");
                    responseMap.put("roomExists", false);
                }
                responseMap.put("message", "Location verified");
                responseMap.put("roomExists", true);
                return ResponseEntity.ok(responseMap);
            } else {
                responseMap.put("message", "The room does not exist at this location");
                responseMap.put("roomExists", false);
                return ResponseEntity.status(400).body(responseMap);
            }
        } catch (Exception e) {
            responseMap.put("message", "Error verifying room location: " + e.getMessage());
            return ResponseEntity.status(500).body(responseMap);
        }
    }

    @Setter
    @Getter
    public static class RoomRequest {
        private int userId;
        private double latitude;
        private double longitude;
    }
}
