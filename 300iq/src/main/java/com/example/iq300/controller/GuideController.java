package com.example.iq300.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; //
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/guide")
@Controller
public class GuideController {

    @GetMapping("") 
    public String safeTradeGuide(Model model) { 
        model.addAttribute("activeMenu", "guide"); 
        return "guide"; 
    }

    @GetMapping("/subscription")
    public String subscriptionGuide(Model model) { 
        model.addAttribute("activeMenu", "guide_sub");
        return "subscription_guide";
    }
    @GetMapping("/fraud")
    public String fraudPreventionGuide(Model model) {
        model.addAttribute("activeMenu", "guide_fraud"); 
        return "guide_fraud"; 
    }
}