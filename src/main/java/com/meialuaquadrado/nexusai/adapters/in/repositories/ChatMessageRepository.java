package com.meialuaquadrado.nexusai.adapters.in.repositories;

import com.meialuaquadrado.nexusai.adapters.in.ChatMessage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatSessionIdOrderByTimestampAsc(Long chatSessionId);
}
