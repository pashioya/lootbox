package lootbox.controller;

import lombok.AllArgsConstructor;
import lootbox.repository.AdvertisementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/advertisements")
public class AdvertisementController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AdvertisementRepository advertisementRepo;

    @GetMapping()
    public String showAllAds (Model model) {
        logger.info("Showing All Ads");
        model.addAttribute("advertisements", advertisementRepo.findAll());
        return "index";
    }
}
