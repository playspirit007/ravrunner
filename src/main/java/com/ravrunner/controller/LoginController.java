package com.ravrunner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

// rendert src/main/resources/templates/login.html
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
