package com.mlb.mlbportal.configuration;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorHandler {

    @RequestMapping("/error")
    public String handleError() {
        return "forward:/index.html";
    }
}