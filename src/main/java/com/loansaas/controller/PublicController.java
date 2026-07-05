package com.loansaas.controller;

import com.loansaas.dto.LenderRegistrationDto;
import com.loansaas.service.LenderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PublicController {

    private final LenderService lenderService;

    @GetMapping("/")
    public String landing() {
        return "landing";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "auth/access-denied";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("registration")) {
            model.addAttribute("registration", new LenderRegistrationDto());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registration") LenderRegistrationDto dto,
                           BindingResult result,
                           RedirectAttributes ra,
                           Model model) {

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (lenderService.phoneExists(dto.getPhone())) {
            result.rejectValue("phone", "exists", "This phone number is already registered");
        }
        if (lenderService.emailExists(dto.getEmail())) {
            result.rejectValue("email", "exists", "This email is already registered");
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        lenderService.register(dto);
        ra.addFlashAttribute("success",
                "Registration successful! Your account is pending Super Admin approval. "
                        + "You will be able to log in once approved.");
        return "redirect:/login";
    }
}
