package com.loansaas.controller;

import com.loansaas.dto.PaymentDto;
import com.loansaas.entity.Lender;
import com.loansaas.service.LoanService;
import com.loansaas.service.PaymentService;
import com.loansaas.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/lender/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final LoanService loanService;
    private final UserService userService;

    private void addCommon(Model model) {
        model.addAttribute("currentUser", userService.getCurrentUser());
        model.addAttribute("lender", userService.getCurrentLender());
        model.addAttribute("active", "payments");
    }

    @GetMapping
    public String list(Model model) {
        addCommon(model);
        Lender lender = userService.getCurrentLender();
        model.addAttribute("payments", paymentService.listForLender(lender));
        return "lender/payments/list";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) Long loanId, Model model) {
        addCommon(model);
        Lender lender = userService.getCurrentLender();
        PaymentDto dto = new PaymentDto();
        dto.setPaymentDate(LocalDate.now());
        if (loanId != null) {
            dto.setLoanId(loanId);
        }
        if (!model.containsAttribute("payment")) {
            model.addAttribute("payment", dto);
        }
        model.addAttribute("loans", loanService.listForLender(lender));
        return "lender/payments/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("payment") PaymentDto dto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        addCommon(model);
        Lender lender = userService.getCurrentLender();
        if (result.hasErrors()) {
            model.addAttribute("loans", loanService.listForLender(lender));
            return "lender/payments/form";
        }
        try {
            paymentService.record(dto, lender, userService.getCurrentUser());
            ra.addFlashAttribute("success", "Payment recorded successfully.");
            return "redirect:/lender/loans/" + dto.getLoanId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("loans", loanService.listForLender(lender));
            return "lender/payments/form";
        }
    }
}
