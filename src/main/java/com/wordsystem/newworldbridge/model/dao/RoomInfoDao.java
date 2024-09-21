package com.wordsystem.newworldbridge.model.dao;

import com.wordsystem.newworldbridge.dto.RoomInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface RoomInfoDao {

    // Set Room
    @Insert("INSERT INTO room_info (id, in_game, room_location_longtitude, room_location_latitude) VALUES (#{id}, #{inGame}, #{roomLocationLongitude}, #{roomLocationLatitude})")
    void setRoom(RoomInfo roomInfo);

    // Update Room
    @Update("UPDATE room_info SET room_location_longtitude = #{roomLocationLongitude}, room_location_latitude = #{roomLocationLatitude} WHERE id = #{id}")
    void updateRoom(RoomInfo roomInfo);

    // Get All Rooms with explicit column mapping
    @Select("SELECT * FROM room_info")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "inGame", column = "in_game"),
            @Result(property = "roomLocationLongitude", column = "room_location_longtitude"),
            @Result(property = "roomLocationLatitude", column = "room_location_latitude")
    })
    List<RoomInfo> getAllRooms();

    // Get Room by ID with explicit column mapping
    @Select("SELECT * FROM room_info WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "inGame", column = "in_game"),
            @Result(property = "roomLocationLongitude", column = "room_location_longtitude"),
            @Result(property = "roomLocationLatitude", column = "room_location_latitude")
    })
    RoomInfo getRoom(int id);

    // Delete Room
    @Delete("DELETE FROM room_info WHERE id = #{id}")
    void deleteRoom(int id);

    // Set In-Game Status
    @Update("UPDATE room_info SET in_game = #{inGame} WHERE id = #{id}")
    void setInGame(@Param("id") int id, @Param("inGame") int inGame);
}
