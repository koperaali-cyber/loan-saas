package com.loansaas.controller;

import com.loansaas.entity.UserStatus;
import com.loansaas.repository.*;
import com.loansaas.service.ActivityService;
import com.loansaas.service.LenderService;
import com.loansaas.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final LenderService lenderService;
    private final UserService userService;
    private final ActivityService activityService;
    private final LenderRepository lenderRepository;
    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    private final PaymentRepository paymentRepository;

    private void addCommon(Model model) {
        model.addAttribute("currentUser", userService.getCurrentUser());
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        addCommon(model);
        model.addAttribute("totalLenders", lenderRepository.count());
        model.addAttribute("pendingLenders",
                lenderRepository.countByApprovalStatus(UserStatus.PENDING_APPROVAL));
        model.addAttribute("activeLenders",
                lenderRepository.countByApprovalStatus(UserStatus.APPROVED));
        model.addAttribute("suspendedLenders",
                lenderRepository.countByApprovalStatus(UserStatus.SUSPENDED));
        model.addAttribute("totalCustomers", customerRepository.count());
        model.addAttribute("totalLoans", loanRepository.count());
        model.addAttribute("totalIssued", loanRepository.totalIssuedAll());
        model.addAttribute("totalCollected", loanRepository.totalCollectedAll());
        model.addAttribute("recentActivities", activityService.recentAll(10));
        model.addAttribute("pendingList",
                lenderService.findByStatus(UserStatus.PENDING_APPROVAL));
        model.addAttribute("active", "dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/lenders")
    public String lenders(Model model) {
        addCommon(model);
        model.addAttribute("lenders", lenderService.findAll());
        model.addAttribute("active", "lenders");
        return "admin/lenders";
    }

    @GetMapping("/lenders/pending")
    public String pending(Model model) {
        addCommon(model);
        model.addAttribute("lenders", lenderService.findByStatus(UserStatus.PENDING_APPROVAL));
        model.addAttribute("active", "pending");
        return "admin/pending";
    }

    @GetMapping("/lenders/{id}")
    public String lenderDetail(@PathVariable Long id, Model model) {
        addCommon(model);
        var lender = lenderService.findById(id);
        model.addAttribute("lender", lender);
        model.addAttribute("customerCount", customerRepository.countByLender(lender));
        model.addAttribute("loanCount", loanRepository.countByLender(lender));
        model.addAttribute("totalIssued", loanRepository.totalIssuedByLender(lender));
        model.addAttribute("totalCollected", loanRepository.totalCollectedByLender(lender));
        model.addAttribute("active", "lenders");
        return "admin/lender-detail";
    }

    @PostMapping("/lenders/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        lenderService.approve(id, userService.getCurrentUser().getFullName());
        ra.addFlashAttribute("success", "Lender approved successfully.");
        return "redirect:/admin/lenders/" + id;
    }

    @PostMapping("/lenders/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam String reason,
                         RedirectAttributes ra) {
        lenderService.reject(id, reason, userService.getCurrentUser().getFullName());
        ra.addFlashAttribute("success", "Lender rejected.");
        return "redirect:/admin/lenders/" + id;
    }

    @PostMapping("/lenders/{id}/suspend")
    public String suspend(@PathVariable Long id, RedirectAttributes ra) {
        lenderService.suspend(id, userService.getCurrentUser().getFullName());
        ra.addFlashAttribute("success", "Lender suspended.");
        return "redirect:/admin/lenders/" + id;
    }

    @PostMapping("/lenders/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes ra) {
        lenderService.activate(id, userService.getCurrentUser().getFullName());
        ra.addFlashAttribute("success", "Lender activated.");
        return "redirect:/admin/lenders/" + id;
    }

    @PostMapping("/lenders/{id}/reset-password")
    public String resetPassword(@PathVariable Long id,
                                @RequestParam String newPassword,
                                RedirectAttributes ra) {
        lenderService.resetPassword(id, newPassword, userService.getCurrentUser().getFullName());
        ra.addFlashAttribute("success", "Password reset successfully.");
        return "redirect:/admin/lenders/" + id;
    }

    @PostMapping("/lenders/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        lenderService.delete(id, userService.getCurrentUser().getFullName());
        ra.addFlashAttribute("success", "Lender account deleted.");
        return "redirect:/admin/lenders";
    }

    @GetMapping("/customers")
    public String allCustomers(Model model) {
        addCommon(model);
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("active", "customers");
        return "admin/customers";
    }

    @GetMapping("/loans")
    public String allLoans(Model model) {
        addCommon(model);
        model.addAttribute("loans", loanRepository.findAll());
        model.addAttribute("active", "loans");
        return "admin/loans";
    }

    @GetMapping("/payments")
    public String allPayments(Model model) {
        addCommon(model);
        model.addAttribute("payments", paymentRepository.findAll());
        model.addAttribute("active", "payments");
        return "admin/payments";
    }

    @GetMapping("/activities")
    public String activities(Model model) {
        addCommon(model);
        model.addAttribute("activities", activityService.recentAll(100));
        model.addAttribute("active", "activities");
        return "admin/activities";
    }
}
