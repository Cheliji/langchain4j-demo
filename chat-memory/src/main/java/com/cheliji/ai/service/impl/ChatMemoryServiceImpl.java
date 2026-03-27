package com.cheliji.ai.service.impl;

import com.cheliji.ai.service.ChatMemoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;



@Slf4j
@Service
@AllArgsConstructor
public class ChatMemoryServiceImpl implements ChatMemoryService {

    private final ChatMemoryStore chatMemoryStore ;
    private final StreamingChatLanguageModel streamModel ;

    @Override
    public SseEmitter ChatWithMemory(String userId, String message) {
        // 获取或创建用户专属记忆
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(userId)
                .maxMessages(20)
                .chatMemoryStore(chatMemoryStore)
                .build();


        log.info("用户问题：{}",message);

        SseEmitter emitter = new SseEmitter();
        chatMemory.add(new UserMessage(message));


        streamModel.chat(chatMemory.messages(), new StreamingChatResponseHandler() {
            StringBuilder fullResponse = new StringBuilder();
            @Override
            public void onPartialResponse(String s) {
                log.info("流式响应：{}",s);
                try {
                    emitter.send(SseEmitter.event()
                            .data(s)
                            .name("message"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse chatResponse) {
                log.info("模型回答：{}",chatResponse.aiMessage().text());
                chatMemory.add(chatResponse.aiMessage());
                emitter.complete();
            }

            @Override
            public void onError(Throwable throwable) {
                emitter.completeWithError(throwable);
            }
        });

        return emitter ;
    }
}
