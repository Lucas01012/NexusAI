package com.meialuaquadrado.nexusai.adapters.out.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meialuaquadrado.nexusai.adapters.in.repositories.ChatMessageRepository;

@RestController
@RequestMapping("/chatMessages")
public class ChatMessageController {
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    ObjectMapper objectMapper;
}
