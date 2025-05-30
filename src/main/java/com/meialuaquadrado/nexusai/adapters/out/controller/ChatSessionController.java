package com.meialuaquadrado.nexusai.adapters.out.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meialuaquadrado.nexusai.adapters.in.ChatSession;
import com.meialuaquadrado.nexusai.adapters.in.User;
import com.meialuaquadrado.nexusai.adapters.in.repositories.ChatSessionRepository;
import com.meialuaquadrado.nexusai.adapters.in.repositories.UserRepository;
import com.meialuaquadrado.nexusai.adapters.in.security.CustomUserDetail;
import com.meialuaquadrado.nexusai.adapters.in.security.SecurityUtils;
import com.meialuaquadrado.nexusai.adapters.in.services.ChatService;
import com.meialuaquadrado.nexusai.models.LoginDto;
import com.meialuaquadrado.nexusai.models.AiDTOs.AiResponse;
import com.meialuaquadrado.nexusai.models.AiDTOs.MessageDto;
import com.meialuaquadrado.nexusai.models.ChatSessionDtos.InsertSessionDto;
import com.meialuaquadrado.nexusai.models.ChatSessionDtos.ChatSessionDto;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/chatSessions")
public class ChatSessionController {
    private ChatSessionRepository chatSessionRepository;
    private UserRepository userRepository;
    private ChatService chatService;
    

    

    @Autowired
    ObjectMapper objectMapper;


    public ChatSessionController(ChatSessionRepository chatSessionRepository, ChatService chatService, UserRepository userRepository)  {
        this.chatSessionRepository = chatSessionRepository;
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Retorna todos as Session do Usuario Logado")
    @GetMapping("/list")
    public ResponseEntity<?> listUserSessions() {
        Long userId;
        try {
            userId = SecurityUtils.getCurrentUser().getId();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("message", "Usuário não autenticado"));
        }

        List<ChatSession> userSessions = chatSessionRepository.findByUserId(userId);

        List<ChatSessionDto> sessionDtos = userSessions.stream()
                .map(session -> new ChatSessionDto(session.getId(), session.getSessionTitle(), session.getCreatedAt()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(sessionDtos);
    }

    @Operation(summary = "Inseri uma nova session para o ususario logado")
    @PostMapping("/insert")
    public ResponseEntity<?> createSession(@RequestBody String tittle) {
        Long userId;
        try {
            userId = SecurityUtils.getCurrentUser().getId();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("message", "Usuário não autenticado"));
        }

        ChatSession newSession = new ChatSession();
        
        Optional<User> sessionUser = userRepository.findById(userId);
        
        if (sessionUser.isPresent()) {
            newSession.setUser(sessionUser.get());
        } else {
            return ResponseEntity.badRequest()
                             .body(Map.of("message", "Usuario não encontrado!"));
        }


        newSession.setSessionTitle(tittle);
        newSession.setCreatedAt(LocalDateTime.now());
        
        try{
            ChatSession savedSession = chatSessionRepository.save(newSession);
            ChatSessionDto chatsession = new ChatSessionDto(savedSession.getUser().getId(), savedSession.getSessionTitle(), savedSession.getCreatedAt());

            return ResponseEntity.ok().body(chatsession);
        }
        catch (Exception e){
             return ResponseEntity.badRequest()
                             .body(Map.of("message", "Erro ao criar nova sessão: " + e.getMessage()));
        }
    }

    @Operation(summary = "Edita o titula da sessão")
    @PutMapping("/edit/{sessionId}")
    public ResponseEntity<?> editSession(@PathVariable Long sessionId, @RequestBody String newTitle) {
        Long userId;
        try {
            userId = SecurityUtils.getCurrentUser().getId();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("message", "Usuário não autenticado"));
        }

        Optional<ChatSession> sessionOpt = chatSessionRepository.findById(sessionId);

        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("message", "Sessão não encontrada"));
        }

        ChatSession session = sessionOpt.get();

        if (!session.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("message", "Você não tem permissão para editar esta sessão"));
        }

        session.setSessionTitle(newTitle);
        chatSessionRepository.save(session);

        ChatSessionDto sessionDto = new ChatSessionDto(session.getId(), session.getSessionTitle(), session.getCreatedAt());

        return ResponseEntity.ok(sessionDto);
    }

    @Operation(summary = "Deleta sessão pelo ID")
    @DeleteMapping("/delete/{sessionId}")
    public ResponseEntity<?> deleteSession(@PathVariable Long sessionId) {
        Long userId;
        try {
            userId = SecurityUtils.getCurrentUser().getId();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("message", "Usuário não autenticado"));
        }

        Optional<ChatSession> sessionOpt = chatSessionRepository.findById(sessionId);

        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("message", "Sessão não encontrada"));
        }

        ChatSession session = sessionOpt.get();

        if (!session.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("message", "Você não tem permissão para deletar esta sessão"));
        }

        chatSessionRepository.delete(session);

        return ResponseEntity.ok(Map.of("message", "Sessão deletada com sucesso"));
    }


    @Operation(summary = "Conversa com chatbot. Em desenvolvimento ainda.")
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>>  login(@RequestBody MessageDto dto) {
        Long userId;
        try {
            userId = SecurityUtils.getCurrentUser().getId();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("message", "Usuário não autenticado"));
        }


        Map<String, String> response = new HashMap<>();

        String aiResponse = chatService.callOpenRouter(dto.getContent(), dto.getModel());
        
        response.put("resposta", aiResponse );
        return ResponseEntity.ok(response);
    }

}
