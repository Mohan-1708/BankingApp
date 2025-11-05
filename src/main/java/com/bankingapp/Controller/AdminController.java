package com.bankingapp.Controller;

import com.bankingapp.Dto.DepositDto;
import com.bankingapp.Model.Account;
import com.bankingapp.Service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AccountService accountService;

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        // Pass an empty DTO for the deposit form
        if (!model.containsAttribute("depositDto")) {
            model.addAttribute("depositDto", new DepositDto());
        }
        return "admin/dashboard"; // -> resources/templates/admin/dashboard.html
    }

    @GetMapping("/search")
    public String searchAccounts(@RequestParam(value = "query", required = false) String query,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        // Add empty DTO for the deposit form
        model.addAttribute("depositDto", new DepositDto());

        if (query == null || query.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Please enter a search term.");
            return "redirect:/admin/dashboard";
        }

        try {
            List<Account> accounts = accountService.searchAccounts(query);
            if (accounts.isEmpty()) {
                model.addAttribute("info", "No accounts found for query: " + query);
            }
            model.addAttribute("accounts", accounts);
        } catch (Exception e) {
            model.addAttribute("error", "Error during search: " + e.getMessage());
        }

        return "admin/dashboard";
    }

    @PostMapping("/deposit")
    public String depositToAccount(@ModelAttribute DepositDto depositDto,
                                   RedirectAttributes redirectAttributes) {
        try {
            accountService.depositToAccount(
                    depositDto.getToAccountNumber(),
                    depositDto.getAmount(),
                    depositDto.getDescription()
            );
            redirectAttributes.addFlashAttribute("success", "Deposit successful!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Deposit failed: " + e.getMessage());
        }

        // Add an empty DTO back to the model in case of redirect
        redirectAttributes.addFlashAttribute("depositDto", new DepositDto());
        return "redirect:/admin/dashboard";
    }
}