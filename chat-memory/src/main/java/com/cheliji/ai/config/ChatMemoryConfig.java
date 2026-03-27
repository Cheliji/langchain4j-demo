package com.cheliji.ai.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    @Bean
    public ChatMemoryStore chatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    @Bean
    public ChatMemory chatMemory(ChatMemoryStore chatMemoryStore) {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .chatMemoryStore(chatMemoryStore)
                .build();
    }



}
