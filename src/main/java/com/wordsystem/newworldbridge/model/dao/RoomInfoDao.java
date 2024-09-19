package com.wordsystem.newworldbridge.model.dao;

import com.wordsystem.newworldbridge.dto.RoomInfo;
import org.apache.ibatis.annotations.*;

public interface RoomInfoDao {

    // Set Room
    @Insert("INSERT INTO room_info (id, in_game, room_location_longtitude, room_location_latitude) VALUES (#{id}, #{inGame}, #{roomLocationLongitude}, #{roomLocationLatitude})")
    void setRoom(RoomInfo roomInfo);

    // Get Room
    @Select("SELECT * FROM room_info WHERE id = #{id}")
    RoomInfo getRoom(int id);

    // Delete Room
    @Delete("DELETE FROM room_info WHERE id = #{id}")
    void deleteRoom(int id);

    // Set In-Game Status
    @Update("UPDATE room_info SET in_game = #{inGame} WHERE id = #{id}")
    void setInGame(@Param("id") int id, @Param("inGame") int inGame);
}
