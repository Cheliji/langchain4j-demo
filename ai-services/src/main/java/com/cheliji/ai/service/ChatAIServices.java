package com.cheliji.ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface ChatAIServices {

    @SystemMessage("你是一名专业的 Java 开发工程师，根据你的专业知识回答用户问题")
    String chat(String message);

}
