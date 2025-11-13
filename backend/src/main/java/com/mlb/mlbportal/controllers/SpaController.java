package com.mlb.mlbportal.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping("/{path:[^\\.]*}")
    public String redirectSingle() {
        return "forward:/index.html";
    }

    @RequestMapping("/**/{path:[^\\.]*}")
    public String redirectMulti() {
        return "forward:/index.html";
    }
}