package com.cheliji.ai.service;

import dev.langchain4j.service.spring.AiService;

@AiService
public interface ToolCallingService {

    String chat(String message);

}
