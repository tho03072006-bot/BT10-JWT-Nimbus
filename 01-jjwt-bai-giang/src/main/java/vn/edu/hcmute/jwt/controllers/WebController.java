package vn.edu.hcmute.jwt.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.hcmute.jwt.configs.DemoAccounts;

/** Buoc 10: tra ve view login.html va profile.html (Thymeleaf). Du lieu lay qua Ajax kem JWT. */
@Controller
@RequestMapping("/")
public class WebController {

    private final boolean demoEnabled;

    public WebController(@Value("${app.demo.enabled:true}") boolean demoEnabled) {
        this.demoEnabled = demoEnabled;
    }

    @GetMapping
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("login")
    public String index(Model model) {
        model.addAttribute("demoAccounts", demoEnabled ? DemoAccounts.ACCOUNTS : java.util.List.of());
        return "login";
    }

    @GetMapping("user/profile")
    public String profile() {
        return "profile";
    }
}
