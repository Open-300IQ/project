package com.example.iq300.model;

public class MessageModel {
	private String role;
	private String text;
	
	public MessageModel(String role, String text) {
		this.role = role;
		this.text = text;
	}
	
	public String getRole() {
		return role;
	}
	
	public String getText() {
		return text;
	}
}
