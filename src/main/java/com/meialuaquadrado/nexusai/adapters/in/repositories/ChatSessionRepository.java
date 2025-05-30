package com.meialuaquadrado.nexusai.adapters.in.repositories;

import com.meialuaquadrado.nexusai.adapters.in.ChatSession;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByUserId(Long userId);


}
