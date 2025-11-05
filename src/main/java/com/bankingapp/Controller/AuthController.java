package com.bankingapp.Controller;

import com.bankingapp.Config.JwtUtil;
import com.bankingapp.Dto.LoginRequestDto;
import com.bankingapp.Dto.UserRegistrationDto;
import com.bankingapp.Service.AccountService;
import com.bankingapp.Service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority; // <-- IMPORT THIS
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {
    // ... (fields are unchanged) ...
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // ... (login/register GET methods are unchanged) ...
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Returns login.html
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute LoginRequestDto loginRequest,
                            HttpServletResponse response,
                            RedirectAttributes redirectAttributes) {
        try {
            // 1. Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            // 2. Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Generate JWT
            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            final String jwt = jwtUtil.generateToken(userDetails);

            // 4. Create and set the HttpOnly cookie
            Cookie jwtCookie = new Cookie("jwt-token", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false); // Set to true in production (HTTPS)
            jwtCookie.setPath("/");
            // Set cookie expiry to 10 minutes (10 * 60 seconds)
            jwtCookie.setMaxAge(10 * 60);
            response.addCookie(jwtCookie);

            // 5. --- NEW: DYNAMIC REDIRECT ---
            // Check the user's authority (role) and redirect
            String redirectUrl = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .map(authority -> {
                        if (authority.equals("ROLE_ADMIN")) {
                            return "redirect:/admin/dashboard";
                        } else {
                            return "redirect:/dashboard";
                        }
                    })
                    .orElse("redirect:/login?error"); // Fallback

            return redirectUrl;

        } catch (Exception e) {
            // 6. Handle bad credentials
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register"; // Returns register.html
    }

    // ... (register POST method is unchanged) ...
    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserRegistrationDto registrationDto,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(
                    registrationDto.getName(),
                    registrationDto.getEmail(),
                    registrationDto.getPassword()
            );
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}