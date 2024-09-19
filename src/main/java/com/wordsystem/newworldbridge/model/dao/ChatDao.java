package com.wordsystem.newworldbridge.model.dao;

import com.wordsystem.newworldbridge.dto.Chat;
import org.apache.ibatis.annotations.*;

public interface ChatDao {

    // Set Chat
    @Insert("INSERT INTO chat (chat_id, user_sender_id, user_receiver_id, chat_log_url) VALUES (#{chatId}, #{userSenderId}, #{userReceiverId}, #{chatLogUrl})")
    void setChat(Chat chat);

    // Get Chat
    @Select("SELECT * FROM chat WHERE chat_id = #{chatId}")
    Chat getChat(int chatId);

    // Update Chat
    @Update("UPDATE chat SET chat_log_url = #{chatLogUrl} WHERE chat_id = #{chatId}")
    void updateChat(@Param("chatId") int chatId, @Param("chatLogUrl") String chatLogUrl);

    // Delete Chat
    @Delete("DELETE FROM chat WHERE chat_id = #{chatId}")
    void deleteChat(int chatId);
}
