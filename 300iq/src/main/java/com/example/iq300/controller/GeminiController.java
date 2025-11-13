package com.example.iq300.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.iq300.service.GeminiService;

@Controller
public class GeminiController {
	private final GeminiService geminiService;
	
	public GeminiController(GeminiService geminiService) {
		this.geminiService = geminiService;
	}
	
	@GetMapping("/ai")
	public String chatPage(Model model) {
		model.addAttribute("chatHistory", geminiService.getChatHistory());
		return "ai";
	}
	
	@PostMapping("/ai/chat")
	@ResponseBody
	public String sendMessage(@RequestBody String message) {
		String cleanMsg = message.replace("{\"message\":\"", "").replace("\"}", "");
		return geminiService.sendMessage(cleanMsg);
	}
}
