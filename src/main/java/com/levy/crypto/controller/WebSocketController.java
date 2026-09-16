package com.levy.crypto.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/hello")
    @SendTo("/topic/messages")
    public String hello(String message) {

        System.out.println("HELLO METHOD WAS CALLED!");

        return "Hello from Spring Boot!";
    }
}