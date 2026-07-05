package com.loansaas.controller;

import com.loansaas.dto.CustomerDto;
import com.loansaas.entity.Customer;
import com.loansaas.entity.Lender;
import com.loansaas.service.CustomerService;
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

@Controller
@RequestMapping("/lender/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final LoanService loanService;
    private final PaymentService paymentService;
    private final UserService userService;

    private void addCommon(Model model) {
        model.addAttribute("currentUser", userService.getCurrentUser());
        model.addAttribute("lender", userService.getCurrentLender());
        model.addAttribute("active", "customers");
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        addCommon(model);
        Lender lender = userService.getCurrentLender();
        model.addAttribute("customers", customerService.search(lender, q));
        model.addAttribute("q", q);
        return "lender/customers/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        addCommon(model);
        if (!model.containsAttribute("customer")) {
            model.addAttribute("customer", new CustomerDto());
        }
        return "lender/customers/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("customer") CustomerDto dto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        addCommon(model);
        if (result.hasErrors()) {
            return "lender/customers/form";
        }
        try {
            Lender lender = userService.getCurrentLender();
            Customer c = customerService.create(dto, lender, userService.getCurrentUser());
            ra.addFlashAttribute("success", "Customer registered successfully.");
            return "redirect:/lender/customers/" + c.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "lender/customers/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        addCommon(model);
        Lender lender = userService.getCurrentLender();
        Customer customer = customerService.getForLender(id, lender);
        model.addAttribute("customer", customer);
        model.addAttribute("loans", loanService.listForCustomer(customer));
        model.addAttribute("riskLevel", customerService.riskLevel(customer));
        model.addAttribute("loanCount", customerService.loanCount(customer));
        model.addAttribute("paidCount", customerService.paidCount(customer));
        model.addAttribute("lateCount", customerService.lateCount(customer));
        return "lender/customers/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        addCommon(model);
        Lender lender = userService.getCurrentLender();
        Customer customer = customerService.getForLender(id, lender);
        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setFullName(customer.getFullName());
        dto.setPhone(customer.getPhone());
        dto.setAlternativePhone(customer.getAlternativePhone());
        dto.setNida(customer.getNida());
        dto.setExistingPhoto(customer.getPhoto());
        model.addAttribute("customer", dto);
        model.addAttribute("editing", true);
        return "lender/customers/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("customer") CustomerDto dto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        addCommon(model);
        model.addAttribute("editing", true);
        if (result.hasErrors()) {
            return "lender/customers/form";
        }
        try {
            Lender lender = userService.getCurrentLender();
            customerService.update(id, dto, lender, userService.getCurrentUser());
            ra.addFlashAttribute("success", "Customer updated successfully.");
            return "redirect:/lender/customers/" + id;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "lender/customers/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Lender lender = userService.getCurrentLender();
            customerService.delete(id, lender, userService.getCurrentUser());
            ra.addFlashAttribute("success", "Customer deleted.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lender/customers";
    }
}
