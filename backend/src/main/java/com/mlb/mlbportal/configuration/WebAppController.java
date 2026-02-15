package com.mlb.mlbportal.configuration;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebAppController {

    @RequestMapping(value = {
            "{path:(?!api)[^\\.]*}",
            "/**/{path:(?!api)[^\\.]*}"
    })
    public String forward(@PathVariable("path") String unused) {
        return "forward:/index.html";
    }
}