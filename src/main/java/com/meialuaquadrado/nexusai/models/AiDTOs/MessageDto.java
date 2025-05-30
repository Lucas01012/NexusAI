package com.meialuaquadrado.nexusai.models.AiDTOs;

public class MessageDto {
    private String role;
    private String content;
    private String  model;
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}
