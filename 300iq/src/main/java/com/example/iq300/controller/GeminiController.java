package com.example.iq300.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.iq300.service.GptService;

@Controller
public class GeminiController {
	private final GptService geminiService;
	
	public GeminiController(GptService geminiService) {
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
	
	@PostMapping("/ai/recommend")
	@ResponseBody
	public String recommendMessage(@RequestBody String message) {
		String cleanMsg = message.replace("{\"message\":\"", "").replace("\"}", "");
		return geminiService.recommendBoardQuestions(cleanMsg);
	}
	
	@PostMapping("/ai/policy")
	@ResponseBody
	public String policyMessage(@RequestBody String message) {
		String cleanMsg = message.replace("{\"message\":\"", "").replace("\"}", "");
		return geminiService.searchPolicy(cleanMsg);
	}
	
	@PostMapping("/ai/term")
	@ResponseBody
	public String termMessage(@RequestBody String message) {
		String cleanMsg = message.replace("{\"message\":\"", "").replace("\"}", "");
		return geminiService.searchTerm(cleanMsg);
	}
}
