// RoomInfoController.java
package com.wordsystem.newworldbridge.controller;

import com.wordsystem.newworldbridge.dto.RoomInfo;
import com.wordsystem.newworldbridge.dto.RoomStatusInfo;
import com.wordsystem.newworldbridge.dto.UserInformation;
import com.wordsystem.newworldbridge.model.Message;
import com.wordsystem.newworldbridge.model.Status;
import com.wordsystem.newworldbridge.model.service.LoginService;
import com.wordsystem.newworldbridge.model.service.RoomInfoService;
import com.wordsystem.newworldbridge.model.service.RoomStatusInfoService;
import com.wordsystem.newworldbridge.model.service.UserInformationService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<RoomInfo>> getAllRooms() {
        try {
            List<RoomInfo> rooms = roomInfoService.getAllRooms();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/room/{hostId}/status")
    public ResponseEntity<RoomStatusInfo> getRoomStatus(@PathVariable int hostId) {
        try {
            RoomStatusInfo roomStatusInfo = roomStatusInfoService.getRoomStatusInfoById(hostId);
            if (roomStatusInfo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(roomStatusInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



    @GetMapping("/room/{roomId}/enter")
    public ResponseEntity<String> updateEnteredPlayerId(
            @PathVariable int roomId,
            @RequestParam("enteredPlayerId") int enteredPlayerId) {
        System.out.println("Updating enteredPlayerId for roomId: " + roomId + ", enteredPlayerId: " + enteredPlayerId);

        try {
            System.out.println("Updating enteredPlayerId for roomId: " + roomId + ", enteredPlayerId: " + enteredPlayerId);

            // Fetch RoomStatusInfo using roomId
            RoomStatusInfo roomStatusInfo = roomStatusInfoService.getRoomStatusInfoById(roomId);
            if (roomStatusInfo == null) {
                System.err.println("RoomStatusInfo not found for roomId: " + roomId);
                return ResponseEntity.status(404).body("Room not found");
            }
            System.out.println("Before update, RoomStatusInfo: " + roomStatusInfo);

            roomStatusInfo.setEnteredPlayerId(enteredPlayerId == 0 ? null : enteredPlayerId);
            roomStatusInfoService.updateRoomStatusInfo(roomStatusInfo);

            System.out.println("After update, RoomStatusInfo: " + roomStatusInfo);

            return ResponseEntity.ok("Entered player ID updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating room status: " + e.getMessage());
            return ResponseEntity.status(500).body("Error updating room status: " + e.getMessage());
        }
    }

    // RoomInfoController.java

    @PutMapping("/room/{hostId}/ready")
    public ResponseEntity<String> updateReadyStatus(
            @PathVariable int hostId,
            @RequestBody Map<String, Object> requestBody) {

        try {
            Object userIdObj = requestBody.get("userId");
            Integer userId = null;
            System.out.println("Received userId: " + userIdObj);

            if (userIdObj instanceof Integer) {
                userId = (Integer) userIdObj;
            } else if (userIdObj instanceof Number) {
                userId = ((Number) userIdObj).intValue();
            } else if (userIdObj instanceof String) {
                userId = Integer.parseInt((String) userIdObj);
            } else {
                System.err.println("Invalid userId type: " + userIdObj);
                return ResponseEntity.status(400).body("Invalid userId");
            }
            Boolean forceReadyStatus = (Boolean) requestBody.get("forceReadyStatus"); // Optional parameter

            System.out.println("Received request to update ready status:");
            System.out.println("Host ID: " + hostId);
            System.out.println("User ID: " + userId);
            System.out.println("Force Ready Status: " + forceReadyStatus);

            // Fetch room status info for the host
            RoomStatusInfo roomStatusInfo = roomStatusInfoService.getRoomStatusInfoById(hostId);
            if (roomStatusInfo == null) {
                System.out.println("Room not found for Host ID: " + hostId);
                return ResponseEntity.status(404).body("Room not found");
            }

            System.out.println("Current Room Status Info: " + roomStatusInfo);

            // Check if the user is the host
            if (roomStatusInfo.getId().equals(userId)) {
                if (forceReadyStatus != null) {
                    // Set host readiness to the forced value
                    roomStatusInfo.setHostIsReady(forceReadyStatus ? 1 : 0);
                } else {
                    // Toggle host readiness
                    roomStatusInfo.setHostIsReady(roomStatusInfo.getHostIsReady() == 1 ? 0 : 1);
                }
                System.out.println("Updated Host Is Ready to: " + roomStatusInfo.getHostIsReady());
            }
            // Check if the user is the visitor
            else if (roomStatusInfo.getEnteredPlayerId() != null && roomStatusInfo.getEnteredPlayerId().equals(userId)) {
                if (forceReadyStatus != null) {
                    // Set visitor readiness to the forced value
                    roomStatusInfo.setVisitorIsReady(forceReadyStatus ? 1 : 0);
                } else {
                    // Toggle visitor readiness
                    roomStatusInfo.setVisitorIsReady(roomStatusInfo.getVisitorIsReady() == 1 ? 0 : 1);
                }
                System.out.println("Updated Visitor Is Ready to: " + roomStatusInfo.getVisitorIsReady());
            } else {
                System.out.println("User ID " + userId + " is not part of the room.");
                return ResponseEntity.status(400).body("User not part of this room");
            }

            // Update the room status in the database
            roomStatusInfoService.updateRoomStatusInfo(roomStatusInfo);
            System.out.println("Updated Room Status Info: " + roomStatusInfo);

            // Create a message object
            Message statusMessage = new Message();
            statusMessage.setMessage("Ready status updated");
            statusMessage.setUserId(userId); // Include the userId

            // If both host and visitor are ready, set the room as in-game and send READY message
            if (roomStatusInfo.getHostIsReady() == 1 && roomStatusInfo.getVisitorIsReady() == 1) {
                roomInfoService.setInGame(hostId, 1);  // Use hostId as room identifier
                System.out.println("Both users are ready. Room " + hostId + " set to in-game.");

                // Send READY message via WebSocket
                statusMessage.setStatus(Status.READY);

                simpMessagingTemplate.convertAndSend("/room/" + hostId + "/public", statusMessage);
            } else {
                roomInfoService.setInGame(hostId, 0);  // Use hostId as room identifier
                System.out.println("Room " + hostId + " is not in-game.");

                // Send NOT_READY message via WebSocket
                statusMessage.setStatus(Status.NOT_READY);

                simpMessagingTemplate.convertAndSend("/room/" + hostId + "/public", statusMessage);
            }

            return ResponseEntity.ok("Ready status updated successfully");
        } catch (Exception e) {
            System.out.println("Error updating ready status: " + e.getMessage());
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
            // Fetch the room information by roomId
            RoomInfo roomInfo = roomInfoService.getRoom(roomId);
            if (roomInfo == null) {
                responseMap.put("message", "Room not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMap);
            }
            System.out.println(roomInfo.getRoomLocationLongitude());

            // Compare the latitude and longitude
            if (Double.compare(roomInfo.getRoomLocationLatitude(), latitude) == 0 &&
                    Double.compare(roomInfo.getRoomLocationLongitude(), longitude) == 0) {
                responseMap.put("message", "Location verified");
                responseMap.put("roomExists", true);
                return ResponseEntity.ok(responseMap);
            } else {
                responseMap.put("message", "The room does not exist at this location");
                responseMap.put("roomExists", false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
        } catch (Exception e) {
            responseMap.put("message", "Error verifying room location: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    // DTO for receiving the request body
    @Setter
    @Getter
    public static class RoomRequest {
        private int userId;
        private double latitude;
        private double longitude;
    }
}
