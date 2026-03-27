package com.cheliji.ai.controller;


import com.cheliji.ai.pojo.Person;
import com.cheliji.ai.service.ChatAIServices;
import com.cheliji.ai.service.PersonAIServices;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/ai/service")
@AllArgsConstructor
public class AIServicesController {

    private final ChatAIServices chatAIServices;
    private final PersonAIServices personAIServices;

    @GetMapping(value = "/chat")
    public String chat(@RequestParam String message) {
        return chatAIServices.chat(message) ;
    }

    @GetMapping(value = "/get/inform")
    public String inform(@RequestParam String message) {
        Person person = personAIServices.chat(message);

        log.info("用户基本信息{}",person);

        return person.toString();

    }

}
