package com.wordsystem.newworldbridge.model.dao;

import com.wordsystem.newworldbridge.dto.UserInformation;
import org.apache.ibatis.annotations.*;

public interface UserInformationDao {

    // Update User Win
    @Update("UPDATE user_information SET user_win = #{userWin} WHERE id = #{id}")
    void updateUserWin(@Param("id") int id, @Param("userWin") int userWin);

    // Update User Lose
    @Update("UPDATE user_information SET user_lose = #{userLose} WHERE id = #{id}")
    void updateUserLose(@Param("id") int id, @Param("userLose") int userLose);

    // Update User Has Room
    @Update("UPDATE user_information SET user_has_room = #{userHasRoom} WHERE id = #{id}")
    void updateUserHasRoom(@Param("id") int id, @Param("userHasRoom") int userHasRoom);

    // Update User Is Playing
    @Update("UPDATE user_information SET is_user_playing = #{isUserPlaying} WHERE id = #{id}")
    void updateUserIsPlaying(@Param("id") int id, @Param("isUserPlaying") int isUserPlaying);

    // Set User Information
    @Insert("INSERT INTO user_information (id, user_win, user_lose, user_has_room, is_user_playing) VALUES (#{id}, #{userWin}, #{userLose}, #{userHasRoom}, #{isUserPlaying})")
    void setUserInformation(UserInformation userInfo);

    // Get User Information
    @Select("SELECT * FROM user_information WHERE id = #{id}")
    UserInformation getUserInformation(int id);

    // Delete User Information
    @Delete("DELETE FROM user_information WHERE id = #{id}")
    void deleteUserInformation(int id);
}
