package com.example.iq300.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/archive")
public class ArchiveController {

    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @GetMapping("/list")
    public String list(Model model) throws IOException {
        // 1. CSV 파일 목록 가져오기 (resources/csv 폴더)
        Resource[] csvResources = resolver.getResources("classpath:csv/*.csv");
        List<String> csvFiles = new ArrayList<>();
        for (Resource r : csvResources) {
            csvFiles.add(r.getFilename());
        }

        // 2. PDF 파일 목록 가져오기 (resources/static/pdf 폴더)
        Resource[] pdfResources = resolver.getResources("classpath:static/pdf/*.pdf");
        List<String> pdfFiles = new ArrayList<>();
        for (Resource r : pdfResources) {
            pdfFiles.add(r.getFilename());
        }

        model.addAttribute("csvFiles", csvFiles);
        model.addAttribute("pdfFiles", pdfFiles);
        model.addAttribute("activeMenu", "archive");

        return "archive_list";
    }

    // CSV 다운로드 처리 (static이 아니라서 직접 접근 불가하므로 컨트롤러가 필요)
    @GetMapping("/download/csv/{filename}")
    public ResponseEntity<Resource> downloadCsv(@PathVariable("filename") String filename) throws IOException {
        Resource resource = resolver.getResource("classpath:csv/" + filename);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        // 한글 파일명 깨짐 방지
        String encodedFileName = URLEncoder.encode(resource.getFilename(), StandardCharsets.UTF_8.name())
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .body(resource);
    }
}