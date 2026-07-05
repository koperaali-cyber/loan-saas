package com.loansaas.controller;

import com.loansaas.dto.ChangePasswordDto;
import com.loansaas.entity.Lender;
import com.loansaas.entity.LoanStatus;
import com.loansaas.entity.User;
import com.loansaas.repository.CustomerRepository;
import com.loansaas.repository.LoanRepository;
import com.loansaas.service.ActivityService;
import com.loansaas.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/lender")
@RequiredArgsConstructor
public class LenderController {

    private final UserService userService;
    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    private final ActivityService activityService;

    private void addCommon(Model model) {
        User user = userService.getCurrentUser();
        Lender lender = userService.getCurrentLender();
        model.addAttribute("currentUser", user);
        model.addAttribute("lender", lender);
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        addCommon(model);
        Lender lender = userService.getCurrentLender();

        model.addAttribute("totalCustomers", customerRepository.countByLender(lender));
        model.addAttribute("activeLoans",
                loanRepository.countByLenderAndStatus(lender, LoanStatus.ACTIVE)
                        + loanRepository.countByLenderAndStatus(lender, LoanStatus.PARTIALLY_PAID));
        model.addAttribute("completedLoans",
                loanRepository.countByLenderAndStatus(lender, LoanStatus.PAID));
        model.addAttribute("overdueLoans",
                loanRepository.countByLenderAndStatus(lender, LoanStatus.OVERDUE)
                        + loanRepository.countByLenderAndStatus(lender, LoanStatus.DEFAULTED));
        model.addAttribute("totalIssued", loanRepository.totalIssuedByLender(lender));
        model.addAttribute("totalCollected", loanRepository.totalCollectedByLender(lender));

        // Chart data: loan status distribution
        model.addAttribute("chartActive",
                loanRepository.countByLenderAndStatus(lender, LoanStatus.ACTIVE));
        model.addAttribute("chartPartial",
                loanRepository.countByLenderAndStatus(lender, LoanStatus.PARTIALLY_PAID));
        model.addAttribute("chartPaid",
                loanRepository.countByLenderAndStatus(lender, LoanStatus.PAID));
        model.addAttribute("chartOverdue",
                loanRepository.countByLenderAndStatus(lender, LoanStatus.OVERDUE));
        model.addAttribute("chartDefaulted",
                loanRepository.countByLenderAndStatus(lender, LoanStatus.DEFAULTED));

        model.addAttribute("recentActivities",
                activityService.recentForUser(userService.getCurrentUser().getId(), 8));
        model.addAttribute("active", "dashboard");
        return "lender/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        addCommon(model);
        model.addAttribute("active", "profile");
        return "lender/profile";
    }

    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        addCommon(model);
        if (!model.containsAttribute("changePassword")) {
            model.addAttribute("changePassword", new ChangePasswordDto());
        }
        model.addAttribute("active", "change-password");
        return "lender/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("changePassword") ChangePasswordDto dto,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes ra) {
        addCommon(model);
        model.addAttribute("active", "change-password");

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "New passwords do not match");
        }
        if (result.hasErrors()) {
            return "lender/change-password";
        }

        User user = userService.getCurrentUser();
        boolean ok = userService.changePassword(user, dto.getCurrentPassword(), dto.getNewPassword());
        if (!ok) {
            result.rejectValue("currentPassword", "wrong", "Current password is incorrect");
            return "lender/change-password";
        }
        activityService.log(user.getId(), user.getFullName(), "Changed account password");
        ra.addFlashAttribute("success", "Password changed successfully.");
        return "redirect:/lender/change-password";
    }
}
