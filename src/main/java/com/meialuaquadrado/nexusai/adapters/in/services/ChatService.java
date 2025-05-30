package com.meialuaquadrado.nexusai.adapters.in.services;

import java.time.LocalDateTime;
import java.util.List;

import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.meialuaquadrado.nexusai.adapters.in.ChatMessage;
import com.meialuaquadrado.nexusai.adapters.in.ChatSession;
import com.meialuaquadrado.nexusai.adapters.in.repositories.ChatMessageRepository;
import com.meialuaquadrado.nexusai.adapters.in.repositories.ChatSessionRepository;
import com.meialuaquadrado.nexusai.models.AiDTOs.AiRequest;
import com.meialuaquadrado.nexusai.models.AiDTOs.AiResponse;
import com.meialuaquadrado.nexusai.models.AiDTOs.MessageDto;

@Service
public class ChatService {

    @Value("${openrounterbarreto.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    public ChatService(
        RestTemplate restTemplate,
        ChatSessionRepository sessionRepository,
        ChatMessageRepository messageRepository
    ) {
        this.restTemplate = restTemplate;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    public String callOpenRouter(String prompt, String model ) {
        String url = "https://openrouter.ai/api/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        MessageDto message = new MessageDto();
        message.setContent(prompt);
        message.setRole("user");

        AiRequest request = new AiRequest();
        request.setModel(model);
        request.setMessages(List.of(message));

        HttpEntity<AiRequest> entity = new HttpEntity<>(request, headers);
        try {
            
            ResponseEntity<AiResponse> response = restTemplate.postForEntity(url, entity, AiResponse.class);

            return response.getBody().getFirstAnswer();
        } catch (Exception e) {
            return "Deu Errado: " + e;
        }
    }
}
