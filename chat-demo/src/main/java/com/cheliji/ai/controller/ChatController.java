package com.cheliji.ai.controller;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@AllArgsConstructor
public class ChatController {

    private ChatLanguageModel chatLanguageModel;

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return chatLanguageModel.chat(message);
    }

}
