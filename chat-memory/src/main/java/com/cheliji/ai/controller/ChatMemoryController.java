package com.cheliji.ai.controller;


import com.cheliji.ai.service.ChatMemoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/memory")
@AllArgsConstructor
public class ChatMemoryController {

    private final ChatMemoryService chatMemoryService;

    @GetMapping(value = "/chat/{id}",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatWithMemory(@PathVariable("id") String userId,
                                     @RequestParam String message) {
        return chatMemoryService.ChatWithMemory(userId,message) ;
    }

}
