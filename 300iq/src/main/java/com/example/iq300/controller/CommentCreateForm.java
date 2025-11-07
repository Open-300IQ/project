package com.example.iq300.controller;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentCreateForm {
    @NotEmpty(message = "내용은 필수항목입니다.")
    private String content;
}