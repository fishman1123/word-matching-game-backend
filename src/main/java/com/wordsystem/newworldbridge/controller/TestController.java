package com.wordsystem.newworldbridge.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("check/google")
public class TestController {

    @GetMapping
    public Map<String, String> showWorkingLog() {

        // Create a JSON-like structure using a Map
        Map<String, String> response = new HashMap<>();
        response.put("message", "working");

        return response; // This will be automatically converted to JSON
    }
}
