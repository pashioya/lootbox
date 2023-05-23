package lootbox.controller;

import lombok.AllArgsConstructor;
import lootbox.domain.Advertisement;
import lootbox.repository.AdvertisementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/")
public class AdvertisementController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AdvertisementRepository advertisementRepo;

    @GetMapping()
    public ModelAndView showAllAds () {
        logger.info("Showing All Ads");
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("advertisements", advertisementRepo.findAll());
        return modelAndView;
    }
}
