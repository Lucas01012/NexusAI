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


    public String callOpenRouterWithContext(Long sessionId, String userPrompt, String model, int maxTotalChars) {
        String url = "https://openrouter.ai/api/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // Busca a sessão no banco
        ChatSession chatSession = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Chat session not found"));

        // Busca mensagens antigas (ordenadas por timestamp ascendente)
        List<ChatMessage> previousMessages = messageRepository.findByChatSessionIdOrderByTimestampAsc(sessionId);

        // Constrói o contexto respeitando limite de caracteres
        List<MessageDto> contextMessages = new java.util.ArrayList<>();
        int currentCharCount = 0;

        for (ChatMessage msg : previousMessages) {
            String content = msg.getContent();
            int contentLength = content.length();

            if (currentCharCount + contentLength > maxTotalChars) {
                break;  // parar se estourar limite
            }

            MessageDto messageDto = new MessageDto();
            messageDto.setRole(msg.getRole());
            messageDto.setContent(content);
            contextMessages.add(messageDto);

            currentCharCount += contentLength;
        }

        // Adiciona a mensagem atual do usuário
        MessageDto userMessage = new MessageDto();
        userMessage.setRole("user");
        userMessage.setContent(userPrompt);
        contextMessages.add(userMessage);

        // Monta request para a API externa
        AiRequest request = new AiRequest();
        request.setModel(model);
        request.setMessages(contextMessages);

        HttpEntity<AiRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<AiResponse> response = restTemplate.postForEntity(url, entity, AiResponse.class);
            String llmResponse = response.getBody().getFirstAnswer();

            // Salva a mensagem do usuário no banco
            ChatMessage userMsgEntity = new ChatMessage();
            userMsgEntity.setChatSession(chatSession);
            userMsgEntity.setRole("user");
            userMsgEntity.setContent(userPrompt);
            userMsgEntity.setTimestamp(LocalDateTime.now());
            messageRepository.save(userMsgEntity);

            // Salva a resposta da LLM no banco
            ChatMessage llmMsgEntity = new ChatMessage();
            llmMsgEntity.setChatSession(chatSession);
            llmMsgEntity.setRole("assistant");
            llmMsgEntity.setLlmName(model);
            llmMsgEntity.setContent(llmResponse);
            llmMsgEntity.setTimestamp(LocalDateTime.now());
            messageRepository.save(llmMsgEntity);

            return llmResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao chamar LLM: " + e.getMessage();
        }
    }

    public String callOpenRouterWithoutContext(Long sessionId, String prompt, String model) {
        String url = "https://openrouter.ai/api/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        MessageDto userMessage = new MessageDto();
        userMessage.setContent(prompt);
        userMessage.setRole("user");

        AiRequest request = new AiRequest();
        request.setModel(model);
        request.setMessages(List.of(userMessage));

        HttpEntity<AiRequest> entity = new HttpEntity<>(request, headers);

        try {
            // Encontra a sessão no banco
            ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Sessão não encontrada"));

            // Salva a mensagem do usuário
            ChatMessage userChatMessage = new ChatMessage();
            userChatMessage.setChatSession(session);
            userChatMessage.setContent(prompt);
            userChatMessage.setRole("user");
            userChatMessage.setLlmName(model);
            userChatMessage.setTimestamp(LocalDateTime.now());
            messageRepository.save(userChatMessage);

            // Chamada à LLM
            ResponseEntity<AiResponse> response = restTemplate.postForEntity(url, entity, AiResponse.class);
            String aiReply = response.getBody().getFirstAnswer();

            // Salva a resposta da LLM
            ChatMessage aiChatMessage = new ChatMessage();
            aiChatMessage.setChatSession(session);
            aiChatMessage.setContent(aiReply);
            aiChatMessage.setRole("assistant");
            aiChatMessage.setLlmName(model);
            aiChatMessage.setTimestamp(LocalDateTime.now());
            messageRepository.save(aiChatMessage);

            return aiReply;
        } catch (Exception e) {
            return "Deu errado: " + e.getMessage();
        }
    }


}
