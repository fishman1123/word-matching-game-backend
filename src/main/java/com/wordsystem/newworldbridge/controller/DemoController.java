package com.wordsystem.newworldbridge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping()
    public String showGreeting() {
        return "Good to see you";
    }
    @GetMapping("/hello")
    public String showResult() {
        return "Good to see you from hello router";
    }

}
