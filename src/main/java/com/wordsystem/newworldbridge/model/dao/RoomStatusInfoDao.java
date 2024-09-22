// src/model/dao/RoomStatusInfoDao.java
package com.wordsystem.newworldbridge.model.dao;

import com.wordsystem.newworldbridge.dto.RoomStatusInfo;
import org.apache.ibatis.annotations.*;

public interface RoomStatusInfoDao {

    // Insert a new RoomStatusInfo record
    @Insert("INSERT INTO room_status_info (id, entered_player_id, host_is_ready, visitor_is_ready) " +
            "VALUES (#{id}, #{enteredPlayerId}, #{hostIsReady}, #{visitorIsReady})")
    void insertRoomStatusInfo(RoomStatusInfo roomStatusInfo);

    // Retrieve a RoomStatusInfo record by id
    @Select("SELECT * FROM room_status_info WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "enteredPlayerId", column = "entered_player_id"),
            @Result(property = "hostIsReady", column = "host_is_ready"),
            @Result(property = "visitorIsReady", column = "visitor_is_ready")
    })
    RoomStatusInfo getRoomStatusInfoById(@Param("id") Integer id);

    // Update an existing RoomStatusInfo record
    @Update("UPDATE room_status_info SET " +
            "entered_player_id = #{enteredPlayerId}, " +
            "host_is_ready = #{hostIsReady}, " +
            "visitor_is_ready = #{visitorIsReady} " +
            "WHERE id = #{id}")
    void updateRoomStatusInfo(RoomStatusInfo roomStatusInfo);

    // Delete a RoomStatusInfo record by id
    @Delete("DELETE FROM room_status_info WHERE id = #{id}")
    void deleteRoomStatusInfoById(@Param("id") Integer id);
}
