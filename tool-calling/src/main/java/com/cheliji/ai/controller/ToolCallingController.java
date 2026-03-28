package com.cheliji.ai.controller;


import com.cheliji.ai.service.ToolCallingService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tool/calling")
@AllArgsConstructor
public class ToolCallingController {

    private final ToolCallingService toolCallingService;

    @GetMapping("/simple/chat")
    public String simpleChat(@RequestParam String message) {



        return toolCallingService.chat(message) ;
    }

    @GetMapping("chat")
    public String chat(@RequestParam String message) {
        return toolCallingService.chat(message) ;
    }

}
