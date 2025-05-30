package com.meialuaquadrado.nexusai.models.ChatSessionDtos;

import java.time.LocalDateTime;

public class ChatSessionDto {
    private Long id;
    private String sessionTitle;
    private LocalDateTime createdAt;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getSessionTitle() {
        return sessionTitle;
    }
    public void setSessionTitle(String sessionTitle) {
        this.sessionTitle = sessionTitle;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public ChatSessionDto(Long id, String sessionTitle, LocalDateTime createdAt) {
        this.id = id;
        this.sessionTitle = sessionTitle;
        this.createdAt = createdAt;
    }
}
