package com.meialuaquadrado.nexusai.adapters.out.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meialuaquadrado.nexusai.adapters.in.repositories.ChatSessionRepository;
import com.meialuaquadrado.nexusai.adapters.in.services.ChatService;
import com.meialuaquadrado.nexusai.models.LoginDto;
import com.meialuaquadrado.nexusai.models.AiDTOs.AiResponse;
import com.meialuaquadrado.nexusai.models.AiDTOs.MessageDto;

@RestController
@RequestMapping("/chatSessions")
public class ChatSessionController {
    private ChatSessionRepository chatSessionRepository;

    private ChatService chatService;

    

    @Autowired
    ObjectMapper objectMapper;


    public ChatSessionController(ChatSessionRepository chatSessionRepository, ChatService chatService) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatService = chatService;
    }


    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>>  login(@RequestBody MessageDto dto) {
        Map<String, String> response = new HashMap<>();

        String aiResponse = chatService.callGemmaApi(dto.getContent());
        
        response.put("resposta", aiResponse );
        return ResponseEntity.ok(response);
    }

}
