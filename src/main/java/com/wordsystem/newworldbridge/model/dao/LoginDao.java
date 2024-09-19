package com.wordsystem.newworldbridge.model.dao;

import com.wordsystem.newworldbridge.dto.Login;
import org.apache.ibatis.annotations.*;

public interface LoginDao {

    // Set User
    @Insert("INSERT INTO login (social_email, username) VALUES (#{socialEmail}, #{username})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void setUser(Login user);

    // Get User
    @Select("SELECT * FROM login WHERE id = #{id}")
    Login getUser(int id);

    // Update User
    @Update("UPDATE login SET social_email = #{socialEmail}, username = #{username} WHERE id = #{id}")
    void updateUser(Login user);

    // Delete User
    @Delete("DELETE FROM login WHERE id = #{id}")
    void deleteUser(int id);

    // Get ID by Username
    @Select("SELECT id FROM login WHERE username = #{username}")
    int getId(String username);

    // Update ID (Not common but included per request)
    @Update("UPDATE login SET id = #{newId} WHERE id = #{id}")
    void updateId(@Param("id") int id, @Param("newId") int newId);

    // Delete ID (Same as deleteUser)
    @Delete("DELETE FROM login WHERE id = #{id}")
    void deleteId(int id);

    @Select("SELECT id FROM login WHERE social_email = #{email}")
    Integer getIdByEmail(String email);
}
