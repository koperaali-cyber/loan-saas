package com.loansaas.controller;

import com.loansaas.dto.LoanDto;
import com.loansaas.entity.Lender;
import com.loansaas.entity.Loan;
import com.loansaas.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/lender/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final CustomerService customerService;
    private final PaymentService paymentService;
    private final ExportService exportService;
    private final UserService userService;

    private void addCommon(Model model) {
        model.addAttribute("currentUser", userService.getCurrentUser());
        model.addAttribute("lender", userService.getCurrentLender());
        model.addAttribute("active", "loans");
    }

    @GetMapping
    public String list(Model model) {
        addCommon(model);
        Lender lender = userService.getCurrentLender();
        model.addAttribute("loans", loanService.listForLender(lender));
        return "lender/loans/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        addCommon(model);
        Lender lender = userService.getCurrentLender();
        if (!model.containsAttribute("loan")) {
            model.addAttribute("loan", new LoanDto());
        }
        model.addAttribute("customers", customerService.listForLender(lender));
        return "lender/loans/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("loan") LoanDto dto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        addCommon(model);
        Lender lender = userService.getCurrentLender();
        if (result.hasErrors()) {
            model.addAttribute("customers", customerService.listForLender(lender));
            return "lender/loans/form";
        }
        try {
            Loan loan = loanService.create(dto, lender, userService.getCurrentUser());
            ra.addFlashAttribute("success", "Loan created successfully.");
            return "redirect:/lender/loans/" + loan.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("customers", customerService.listForLender(lender));
            return "lender/loans/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        addCommon(model);
        Lender lender = userService.getCurrentLender();
        Loan loan = loanService.getForLender(id, lender);
        model.addAttribute("loan", loan);
        model.addAttribute("payments", paymentService.listForLoan(loan));
        return "lender/loans/detail";
    }

    @PostMapping("/{id}/default")
    public String markDefault(@PathVariable Long id, RedirectAttributes ra) {
        Lender lender = userService.getCurrentLender();
        loanService.markDefaulted(id, lender, userService.getCurrentUser());
        ra.addFlashAttribute("success", "Loan marked as defaulted.");
        return "redirect:/lender/loans/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        Lender lender = userService.getCurrentLender();
        loanService.delete(id, lender, userService.getCurrentUser());
        ra.addFlashAttribute("success", "Loan deleted.");
        return "redirect:/lender/loans";
    }

    // ---- Exports ----
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf() {
        Lender lender = userService.getCurrentLender();
        List<Loan> loans = loanService.listForLender(lender);
        byte[] pdf = exportService.loansToPdf(lender.getBusinessName(), loans);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=loan-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel() {
        Lender lender = userService.getCurrentLender();
        List<Loan> loans = loanService.listForLender(lender);
        byte[] xlsx = exportService.loansToExcel(lender.getBusinessName(), loans);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=loan-report.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }
}
