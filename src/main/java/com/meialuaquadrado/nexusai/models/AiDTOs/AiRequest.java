package com.meialuaquadrado.nexusai.models.AiDTOs;

import java.util.List;

public class AiRequest {
    private String model;
    private List<MessageDto> messages;
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public List<MessageDto> getMessages() {
        return messages;
    }
    public void setMessages(List<MessageDto> messages) {
        this.messages = messages;
    }
}

