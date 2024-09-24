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



    @GetMapping("/room/{hostId}/enter")
    public ResponseEntity<String> updateEnteredPlayerId(
            @PathVariable int hostId,
            @RequestParam("enteredPlayerId") int enteredPlayerId) {
        try {
            RoomStatusInfo roomStatusInfo = roomStatusInfoService.getRoomStatusInfoById(hostId);
            if (roomStatusInfo == null) {
                return ResponseEntity.status(404).body("Room not found");
            }
            roomStatusInfo.setEnteredPlayerId(enteredPlayerId);
            roomStatusInfoService.updateRoomStatusInfo(roomStatusInfo);
            return ResponseEntity.ok("Entered player ID updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating room status: " + e.getMessage());
        }
    }

    // New API: Update ready status when a user clicks ready
    @PutMapping("/room/{hostId}/ready")
    public ResponseEntity<String> updateReadyStatus(
            @PathVariable int hostId,
            @RequestBody Map<String, Integer> requestBody) {
        try {
            Integer userId = requestBody.get("userId");

            // Fetch room status info for the host
            RoomStatusInfo roomStatusInfo = roomStatusInfoService.getRoomStatusInfoById(hostId);
            if (roomStatusInfo == null) {
                return ResponseEntity.status(404).body("Room not found");
            }

            // Check if the user is the host
            if (roomStatusInfo.getId().equals(userId)) {
                // Toggle host readiness
                roomStatusInfo.setHostIsReady(roomStatusInfo.getHostIsReady() == 1 ? 0 : 1);
            }
            // Check if the user is the visitor
            else if (roomStatusInfo.getEnteredPlayerId() != null && roomStatusInfo.getEnteredPlayerId().equals(userId)) {
                // Toggle visitor readiness
                roomStatusInfo.setVisitorIsReady(roomStatusInfo.getVisitorIsReady() == 1 ? 0 : 1);
            }
            else {
                return ResponseEntity.status(400).body("User not part of this room");
            }

            // Update the room status in the database
            roomStatusInfoService.updateRoomStatusInfo(roomStatusInfo);

            // If both host and visitor are ready, set the room as in-game
            if (roomStatusInfo.getHostIsReady() == 1 && roomStatusInfo.getVisitorIsReady() == 1) {
                roomInfoService.setInGame(hostId, 1);  // Use hostId or another room identifier

                UserInformation hostUser = userInformationService.getUserInformation(roomStatusInfo.getId());
                UserInformation visitorUser = userInformationService.getUserInformation(roomStatusInfo.getEnteredPlayerId());
                System.out.println("host user data" + hostUser.getId());
                Message readyMessage = new Message();
                readyMessage.setMessage("true");
                readyMessage.setStatus(Status.valueOf("READY"));


                String hostName = loginService.getUserNameById(hostId);
                String username = loginService.getUserNameById(userId);
                System.out.println(hostName);
                System.out.println(username);



//                simpMessagingTemplate.convertAndSendToUser(hostName, "/ready/" + hostId + "/private", readyMessage);
//                simpMessagingTemplate.convertAndSendToUser(username, "/ready/" + hostId + "/private", readyMessage);
            } else {
                roomInfoService.setInGame(hostId, 0);  // Use hostId or another room identifier

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
