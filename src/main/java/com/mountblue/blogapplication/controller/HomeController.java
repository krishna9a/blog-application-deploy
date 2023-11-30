package com.mountblue.blogapplication.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/showMyLoginPage")
    public String showMyLoginPage(){
        return "login";
    }

    @GetMapping("/signup")
    public String registerUser(){
        return "register";
    }

    @GetMapping("/logout")
    public String logoutUser(){
        return "redirect:/";
    }
}