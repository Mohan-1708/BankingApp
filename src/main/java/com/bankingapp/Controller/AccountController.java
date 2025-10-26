//package com.bankingapp.Controller;
//
//import com.bankingapp.Dto.TransferRequestDto;
//import com.bankingapp.Model.Account;
//import com.bankingapp.Model.Transaction;
//import com.bankingapp.Model.User;
//import com.bankingapp.Service.AccountService;
//import com.bankingapp.Service.UserServiceImpl;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.security.Principal;
//import java.util.List;
//
//@Controller
//@RequiredArgsConstructor
//public class AccountController {
//
//    private final AccountService accountService;
//    private final UserServiceImpl userService; // Used to get User object from email
//
//    @GetMapping("/dashboard")
//    public String showDashboard(Model model, Principal principal) {
//        // Get the logged-in user
//        User user = (User) userService.loadUserByUsername(principal.getName());
//
//        // Get their accounts
//        List<Account> accounts = accountService.getAccountsByUserId(user.getId());
//
//        model.addAttribute("user", user);
//        model.addAttribute("accounts", accounts);
//        return "dashboard"; // Returns dashboard.html
//    }
//
//    @GetMapping("/transfer")
//    public String showTransferForm(Model model, Principal principal) {
//        // Get user's accounts to populate the "From" dropdown
//        User user = (User) userService.loadUserByUsername(principal.getName());
//        List<Account> userAccounts = accountService.getAccountsByUserId(user.getId());
//
//        model.addAttribute("userAccounts", userAccounts);
//
//        // --- THIS IS THE FIX ---
//        // You must add an empty object to the model for the form to bind to
//        model.addAttribute("transferRequest", new TransferRequestDto());
//
//        return "transfer"; // Returns transfer.html
//    }
//
//    @PostMapping("/transfer")
//    public String processTransfer(@ModelAttribute TransferRequestDto transferRequest,
//                                  Principal principal,
//                                  RedirectAttributes redirectAttributes) {
//        try {
//            // Security check: Ensure the "from" account belongs to the logged-in user
//            Account fromAccount = accountService.getAccountByAccountNumber(transferRequest.getFromAccountNumber());
//            if (!fromAccount.getUser().getEmail().equals(principal.getName())) {
//                throw new SecurityException("User is not authorized to transfer from this account.");
//            }
//
//            // Perform the transfer
//            accountService.transferFunds(
//                    transferRequest.getFromAccountNumber(),
//                    transferRequest.getToAccountNumber(),
//                    transferRequest.getAmount(),
//                    transferRequest.getDescription()
//            );
//
//            redirectAttributes.addFlashAttribute("success", "Transfer successful!");
//            return "redirect:/dashboard";
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Transfer failed: " + e.getMessage());
//            return "redirect:/transfer";
//        }
//    }
//
//    @GetMapping("/history")
//    public String showTransactionHistory(@RequestParam("accountId") Long accountId,
//                                         Model model, Principal principal) {
//        try {
//            // Security check: Ensure the user is viewing one of their *own* accounts
//            Account account = accountService.getAccountByIdAndUserEmail(accountId, principal.getName());
//
//            // Get transactions for that specific account
//            List<Transaction> transactions = accountService.getTransactionHistory(accountId);
//
//            model.addAttribute("account", account);
//            model.addAttribute("transactions", transactions);
//            return "history"; // Returns history.html
//
//        } catch (Exception e) {
//            // If user tries to access an account that isn't theirs
//            return "redirect:/dashboard?error=access_denied";
//        }
//    }
//}

package com.bankingapp.Controller;

import com.bankingapp.Dto.TransferRequestDto;
import com.bankingapp.Model.Account;
import com.bankingapp.Model.Transaction;
import com.bankingapp.Model.User;
import com.bankingapp.Service.AccountService;
import com.bankingapp.Service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserServiceImpl userService; // Used to get User object from email

    @GetMapping("/dashboard")
// ... (showDashboard method is unchanged) ...
    public String showDashboard(Model model, Principal principal) {
        // Get the logged-in user
        User user = (User) userService.loadUserByUsername(principal.getName());

        // Get their accounts
        List<Account> accounts = accountService.getAccountsByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        return "dashboard"; // Returns dashboard.html
    }

    @GetMapping("/transfer")
// ... (showTransferForm method is unchanged) ...
    public String showTransferForm(Model model, Principal principal) {
        // Get user's accounts to populate the "From" dropdown
        User user = (User) userService.loadUserByUsername(principal.getName());
        List<Account> userAccounts = accountService.getAccountsByUserId(user.getId());

        model.addAttribute("userAccounts", userAccounts);

        // --- THIS IS THE FIX ---
        // You must add an empty object to the model for the form to bind to
        model.addAttribute("transferRequest", new TransferRequestDto());

        return "transfer"; // Returns transfer.html
    }

    @PostMapping("/transfer")
// ... (processTransfer method is unchanged) ...
    public String processTransfer(@ModelAttribute TransferRequestDto transferRequest,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Security check: Ensure the "from" account belongs to the logged-in user
            Account fromAccount = accountService.getAccountByAccountNumber(transferRequest.getFromAccountNumber());
            if (!fromAccount.getUser().getEmail().equals(principal.getName())) {
                throw new SecurityException("User is not authorized to transfer from this account.");
            }

            // Perform the transfer
            accountService.transferFunds(
                    transferRequest.getFromAccountNumber(),
                    transferRequest.getToAccountNumber(),
                    transferRequest.getAmount(),
                    transferRequest.getDescription()
            );

            redirectAttributes.addFlashAttribute("success", "Transfer successful!");
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Transfer failed: " + e.getMessage());
            return "redirect:/transfer";
        }
    }

    @GetMapping("/history")
    public String showTransactionHistory(@RequestParam("accountId") Long accountId,
                                         Model model, Principal principal) {
        try {
            // Security check: Ensure the user is viewing one of their *own* accounts
            Account account = accountService.getAccountByIdAndUserEmail(accountId, principal.getName());

            // Get transactions for that specific account
            List<Transaction> transactions = accountService.getTransactionHistory(accountId);

            model.addAttribute("account", account);
            model.addAttribute("transactions", transactions);

            // --- THIS IS THE FIX ---
            // Your history.html template seems to require a 'transferRequest' bean
            // (possibly from a copy/pasted fragment)
            model.addAttribute("transferRequest", new TransferRequestDto());

            return "history"; // Returns history.html

        } catch (Exception e) {
            // If user tries to access an account that isn't theirs
            return "redirect:/dashboard?error=access_denied";
        }
    }
}

