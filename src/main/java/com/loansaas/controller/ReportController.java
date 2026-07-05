package com.loansaas.controller;

import com.loansaas.entity.Lender;
import com.loansaas.entity.LoanStatus;
import com.loansaas.repository.LoanRepository;
import com.loansaas.service.LoanService;
import com.loansaas.service.PaymentService;
import com.loansaas.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/lender/reports")
@RequiredArgsConstructor
public class ReportController {

    private final LoanService loanService;
    private final PaymentService paymentService;
    private final LoanRepository loanRepository;
    private final UserService userService;

    @GetMapping
    public String reports(Model model) {
        Lender lender = userService.getCurrentLender();
        model.addAttribute("currentUser", userService.getCurrentUser());
        model.addAttribute("lender", lender);
        model.addAttribute("active", "reports");

        model.addAttribute("loans", loanService.listForLender(lender));
        model.addAttribute("payments", paymentService.listForLender(lender));
        model.addAttribute("totalIssued", loanRepository.totalIssuedByLender(lender));
        model.addAttribute("totalCollected", loanRepository.totalCollectedByLender(lender));
        model.addAttribute("activeLoans",
                loanRepository.countByLenderAndStatus(lender, LoanStatus.ACTIVE));
        model.addAttribute("overdueLoans",
                loanRepository.countByLenderAndStatus(lender, LoanStatus.OVERDUE));
        model.addAttribute("paidLoans",
                loanRepository.countByLenderAndStatus(lender, LoanStatus.PAID));
        return "lender/reports";
    }
}
