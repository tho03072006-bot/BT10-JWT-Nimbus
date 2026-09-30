package vn.edu.hcmute.jwt.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Buoc 10: tra ve view login.html va profile.html (Thymeleaf). Du lieu lay qua Ajax kem JWT. */
@Controller
@RequestMapping("/")
public class WebController {

    @GetMapping
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("login")
    public String index() {
        return "login";
    }

    @GetMapping("user/profile")
    public String profile() {
        return "profile";
    }
}
