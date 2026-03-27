package com.cheliji.ai.controller;


import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.TokenUsage;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/stream")
@AllArgsConstructor
public class StreamChatController {

    private final StreamingChatLanguageModel streamingChatLanguageModel;

    @GetMapping(value = "/chat" , produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam String message) {

        SseEmitter sseEmitter = new SseEmitter();

        streamingChatLanguageModel.chat(message, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String message) {
                try {
                    System.out.println("message: " + message);
                    sseEmitter.send(SseEmitter.event()
                            .data(message, MediaType.APPLICATION_JSON)
                            .name("message"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse chatResponse) {
                TokenUsage tokenUsage = chatResponse.tokenUsage();

                Integer inputTokenCount = tokenUsage.inputTokenCount();
                Integer outputTokenCount = tokenUsage.outputTokenCount();
                Integer totalTokenCount = tokenUsage.totalTokenCount();
                System.out.println("inputTokenCount: " + inputTokenCount);
                System.out.println("outputTokenCount: " + outputTokenCount);
                System.out.println("totalTokenCount: " + totalTokenCount);

                sseEmitter.complete();

            }

            @Override
            public void onError(Throwable throwable) {
                sseEmitter.completeWithError(throwable);
            }
        });

        return sseEmitter;

    }


}
