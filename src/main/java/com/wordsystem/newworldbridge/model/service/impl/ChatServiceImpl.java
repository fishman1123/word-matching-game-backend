package com.wordsystem.newworldbridge.model.service.impl;

import com.wordsystem.newworldbridge.dto.Chat;
import com.wordsystem.newworldbridge.model.dao.ChatDao;
import com.wordsystem.newworldbridge.model.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatDao chatDao;

    @Autowired
    public ChatServiceImpl(ChatDao chatDao) {
        this.chatDao = chatDao;
    }

    @Override
    public void setChat(Chat chat) {
        chatDao.setChat(chat);
    }

    @Override
    public Chat getChat(int chatId) {
        return chatDao.getChat(chatId);
    }

    @Override
    public void updateChat(int chatId, String newLogUrl) {
        chatDao.updateChat(chatId, newLogUrl);
    }

    @Override
    public void deleteChat(int chatId) {
        chatDao.deleteChat(chatId);
    }
}
