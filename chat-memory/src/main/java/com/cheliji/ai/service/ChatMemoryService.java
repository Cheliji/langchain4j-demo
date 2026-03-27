package com.cheliji.ai.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ChatMemoryService {

    SseEmitter ChatWithMemory (String userId,String message) ;

}
