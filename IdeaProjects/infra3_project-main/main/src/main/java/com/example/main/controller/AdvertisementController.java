package com.example.main.controller;

import com.example.main.repository.AdvertisementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/advertisements")
public class AdvertisementController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AdvertisementRepository advertisementRepo;

    public AdvertisementController(AdvertisementRepository advertisementRepo) {
        this.advertisementRepo = advertisementRepo;
    }

    @GetMapping()
    public String showAllAds (Model model) {
        model.addAttribute("ads", advertisementRepo.findAll());
        return "html/advertisements";
    }
}
