package com.meialuaquadrado.nexusai.adapters.in.services;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final int CONST_SIZE_LIMITER = 4;

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

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String buildContextPrompt(List<ChatMessage> messages, String model) {

        // List<Map<String, String>> contextMessages = messages.stream().map(msg -> {
        //     Map<String, String> entry = new LinkedHashMap<>();
        //     entry.put("Sender", msg.getRole());
        //     entry.put("Content", msg.getContent());
        //     return entry;
        // }).collect(Collectors.toList());

        //  int size = contextMessages.size();

        // if (model.equals("google/gemma-3n-e4b-it:free") && size > CONST_SIZE_LIMITER) 
        //     contextMessages =  contextMessages.subList(size - CONST_SIZE_LIMITER, size);

        StringBuilder contextMessages = new StringBuilder();

        for (ChatMessage chatMessage : messages) {
            contextMessages.append(chatMessage.getContent());
        }



        try {
            String jsonContext = objectMapper.writeValueAsString(contextMessages);
            return "You are assuming the position as another LLM, where the following is the context: \n" + jsonContext + "\n";
        } catch (JsonProcessingException e) {
            return "Error generating context JSON: " + e.getMessage();
        }
    }


    public String callOpenRouterWithContext(Long sessionId, String userPrompt, String model) {
        String url = "https://openrouter.ai/api/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // Busca a sessão no banco
        ChatSession chatSession = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Chat session not found"));

        // Busca mensagens antigas (ordenadas por timestamp ascendente)
        List<ChatMessage> previousMessages = messageRepository.findByChatSessionIdOrderByTimestampAsc(sessionId);
        
        String context = buildContextPrompt(previousMessages, model);

        context += "Use the context to continue the conversation, but don't answer it: \n" + userPrompt;
    

        // Adiciona a mensagem atual do usuário
        MessageDto userMessage = new MessageDto();
        userMessage.setRole("user");
        userMessage.setContent(context);

        // Monta request para a API externa
        AiRequest request = new AiRequest();
        request.setModel(model);
        request.setMessages(List.of(userMessage));

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
