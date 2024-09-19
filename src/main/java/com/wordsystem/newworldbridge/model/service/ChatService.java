package com.wordsystem.newworldbridge.model.service;

import com.wordsystem.newworldbridge.dto.Chat;

public interface ChatService {

    // 20. setChat
    void setChat(Chat chat);

    // 21. getChat
    Chat getChat(int chatId);

    // 22. updateChat
    void updateChat(int chatId, String newLogUrl);

    // 23. deleteChat
    void deleteChat(int chatId);
}
