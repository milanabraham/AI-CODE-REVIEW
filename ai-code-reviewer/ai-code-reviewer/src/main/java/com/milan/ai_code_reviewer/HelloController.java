package com.milan.ai_code_reviewer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello Milan! ✅ Your Spring Boot app is running successfully.";
    }
}
