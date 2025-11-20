package com.example.iq300.controller;

import com.example.iq300.domain.HousingPolicy;
import com.example.iq300.service.HousingPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/policy")
@RequiredArgsConstructor
public class HousingPolicyController {

    private final HousingPolicyService housingPolicyService;

    @GetMapping("/list")
    public String list(Model model, @RequestParam(value="page", defaultValue="0") int page,
                       @RequestParam(value="kw", defaultValue="") String kw) {
        Page<HousingPolicy> paging = housingPolicyService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        
        
        model.addAttribute("activeMenu", "policy");
        return "policy_list"; // templates/policy_list.html
    }
    
    @GetMapping("/detail/{id}")
    public String detail(Model model, @PathVariable("id") Long id) {
        HousingPolicy policy = housingPolicyService.getPolicy(id);
        model.addAttribute("policy", policy);
        return "policy_detail"; // templates/policy_detail.html
    }
}