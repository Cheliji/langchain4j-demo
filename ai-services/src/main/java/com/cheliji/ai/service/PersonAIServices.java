package com.cheliji.ai.service;

import com.cheliji.ai.pojo.Person;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface PersonAIServices {

    @UserMessage("从下面语句判断这个人的基本信息{{it}}")
    Person chat(String text) ;

}
