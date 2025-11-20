package com.example.iq300.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.iq300.domain.MapData;
import com.example.iq300.domain.MonthlyAvgPrice;
import com.example.iq300.service.MapService;
import com.example.iq300.service.MonthlyAvgPriceService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class MainController {
	
    @Value("${map.api}")
    private String mapApiKey;
	
    @Autowired
    private MonthlyAvgPriceService monthlyAvgPriceService;
	
    // private final BoardService boardService; // [삭제] 이제 여기서 안 씁니다.
    private final MapService mapService;

    @GetMapping("/")
    public String root() {
        return "redirect:/board/list";
    }

    @GetMapping("/analysis")
    public String analysis(Model model) {
        List<MonthlyAvgPrice> avgPriceList = monthlyAvgPriceService.getDistrictAvgPriceData(); 
        model.addAttribute("avgPriceData", avgPriceList);
        model.addAttribute("activeMenu", "analysis");
        return "analysis";
    }
    
    @GetMapping("/map")
    public String map(Model model) {
        model.addAttribute("activeMenu", "map");
        model.addAttribute("mapApiKey", mapApiKey); 
        
        Map<String, List<String>> districtsAndNeighborhoods = mapService.getUniqueDistrictsAndNeighborhoods();
        List<String> guList = districtsAndNeighborhoods.keySet().stream().sorted().collect(Collectors.toList());
        model.addAttribute("guList", guList);
        model.addAttribute("districtMap", districtsAndNeighborhoods);
        
        List<MapData> allMapData = mapService.getAllMapData();
        model.addAttribute("allMapData", allMapData); 
        
        return "map";
    }

}