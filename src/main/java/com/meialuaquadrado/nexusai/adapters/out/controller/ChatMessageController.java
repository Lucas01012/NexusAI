package com.meialuaquadrado.nexusai.adapters.out.controller;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.meialuaquadrado.nexusai.adapters.in.*;
import com.meialuaquadrado.nexusai.adapters.in.repositories.*;
import com.meialuaquadrado.nexusai.models.ChatMessageDto;
import com.meialuaquadrado.nexusai.adapters.in.security.SecurityUtils;

@RestController
@RequestMapping("/chatMessages")
public class ChatMessageController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    // LIST ALL (do usuário logado)
    @GetMapping
    public ResponseEntity<List<ChatMessageDto>> getAllMessages() {
        Long userId = getCurrentUserId();

        List<ChatMessageDto> messages = chatMessageRepository.findAll().stream()
            .filter(msg -> msg.getChatSession().getUser().getId().equals(userId))
            .map(this::toDto)
            .collect(Collectors.toList());

        return ResponseEntity.ok(messages);
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getMessageById(@PathVariable Long id) {
        Long userId = getCurrentUserId();

        Optional<ChatMessage> messageOpt = chatMessageRepository.findById(id);
        if (messageOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mensagem não encontrada");
        }

        ChatMessage message = messageOpt.get();
        if (!message.getChatSession().getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
        }

        return ResponseEntity.ok(toDto(message));
    }

    // CREATE
    @PostMapping
    public ResponseEntity<?> createMessage(@RequestParam Long sessionId, @RequestBody ChatMessage newMessage) {
        Long userId = getCurrentUserId();

        Optional<ChatSession> sessionOpt = chatSessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sessão não encontrada");
        }

        ChatSession session = sessionOpt.get();
        if (!session.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não pode adicionar mensagem nesta sessão");
        }

        newMessage.setChatSession(session);
        newMessage.setTimestamp(LocalDateTime.now());

        ChatMessage saved = chatMessageRepository.save(newMessage);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMessage(@PathVariable Long id, @RequestBody ChatMessage updatedMessage) {
        Long userId = getCurrentUserId();

        Optional<ChatMessage> existingOpt = chatMessageRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mensagem não encontrada");
        }

        ChatMessage message = existingOpt.get();
        if (!message.getChatSession().getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não pode editar esta mensagem");
        }

        message.setRole(updatedMessage.getRole());
        message.setLlmName(updatedMessage.getLlmName());
        message.setContent(updatedMessage.getContent());

        ChatMessage saved = chatMessageRepository.save(message);
        return ResponseEntity.ok(toDto(saved));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id) {
        Long userId = getCurrentUserId();

        Optional<ChatMessage> existingOpt = chatMessageRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mensagem não encontrada");
        }

        ChatMessage message = existingOpt.get();
        if (!message.getChatSession().getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não pode deletar esta mensagem");
        }

        chatMessageRepository.deleteById(id);
        return ResponseEntity.ok("Mensagem deletada com sucesso");
    }

    // UTILS
    private ChatMessageDto toDto(ChatMessage message) {
        return new ChatMessageDto(
            message.getId(),
            message.getRole(),
            message.getLlmName(),
            message.getContent(),
            message.getTimestamp()
        );
    }

    private Long getCurrentUserId() {
        return SecurityUtils.getCurrentUser().getId();
    }
}
