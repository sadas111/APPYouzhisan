package com.chenghua.service;

import com.chenghua.entity.ChatMessage;
import com.chenghua.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public ChatMessage saveMessage(Long userId, String role, String content) {
        ChatMessage message = ChatMessage.builder()
                .userId(userId)
                .role(role)
                .content(content)
                .build();
        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getHistory(Long userId) {
        return chatMessageRepository.findByUserIdOrderByCreateTimeAsc(userId);
    }
}
