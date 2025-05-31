package com.meialuaquadrado.nexusai.models;

import java.time.LocalDateTime;

public class ChatMessageDto {
    private Long id;
    private String role;
    private String llmName;
    private String content;
    private LocalDateTime timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLlmName() {
        return llmName;
    }

    public void setLlmName(String llmName) {
        this.llmName = llmName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public ChatMessageDto(Long id, String role, String llmName, String content, LocalDateTime timestamp) {
        this.id = id;
        this.role = role;
        this.llmName = llmName;
        this.content = content;
        this.timestamp = timestamp;
    }
}
