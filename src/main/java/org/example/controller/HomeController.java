package org.example.controller;

import org.example.service.HomeService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class HomeController {
    private final HomeService homeService;


    public HomeController(HomeService homeService) {
        this.homeService = homeService;

    }

    @GetMapping("/test")
    public String testEndPoint(){
        return "Force is where there is need";
    }
    @CrossOrigin("*")
    @GetMapping("/modelTest")
    public String responseFromModel(@RequestParam(name = "managerName") String managerName,
                                    @RequestParam(name = "leaveDate") String leaveDate,
                                    @RequestParam(name = "excuseLevel") String excuseLevel) {
        return homeService.getResponseFromGeminiModel(managerName, leaveDate, excuseLevel);
    }





}