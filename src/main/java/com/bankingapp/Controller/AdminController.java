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

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AccountService accountService;

    /**
     * Handles the main admin dashboard page.
     * It fetches stats and a list of all accounts (or search results)
     * and adds them to the model.
     */
    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model, @RequestParam(value = "query", required = false) String query) {

        // 1. Add Stats to the model
        model.addAttribute("totalCustomers", accountService.getTotalCustomerCount());
        model.addAttribute("totalAccounts", accountService.getTotalAccountCount());
        model.addAttribute("totalBalance", accountService.getTotalBankBalance());

        // 2. Add an empty DTO for the deposit form
        if (!model.containsAttribute("depositDto")) {
            model.addAttribute("depositDto", new DepositDto());
        }

        // 3. Handle search or view all accounts
        List<Account> accounts;
        if (query != null && !query.isBlank()) {
            // A search query exists
            accounts = accountService.searchAccounts(query);
            if (accounts.isEmpty()) {
                model.addAttribute("info", "No accounts found for query: " + query);
            }
        } else {
            // No search, so show all accounts by default
            accounts = accountService.findAllAccounts();
        }

        model.addAttribute("accounts", accounts);
        return "admin/dashboard"; // Renders admin/dashboard.html
    }

    /**
     * Handles the deposit form submission from the admin dashboard.
     */
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
        return "redirect:/admin/dashboard";
    }
}