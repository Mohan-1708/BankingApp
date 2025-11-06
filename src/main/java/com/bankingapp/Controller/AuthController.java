package com.bankingapp.Controller;

import com.bankingapp.Config.JwtUtil;
import com.bankingapp.Dto.LoginRequestDto;
import com.bankingapp.Dto.UserRegistrationDto;
import com.bankingapp.Service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // --- USER LOGIN ---
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Renders templates/login.html
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute LoginRequestDto loginRequest,
                            HttpServletResponse response,
                            RedirectAttributes redirectAttributes) {

        Authentication authentication;
        try {
            // 1. Try to authenticate
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
        } catch (Exception e) {
            // 2. If auth fails, redirect back with an error
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/login";
        }

        // 3. Set authentication in Spring Security's context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4. ROLE CHECK: Only allow ROLE_USER to log in here
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse(null);

        if (!"ROLE_USER".equals(role)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. This login is for customers only.");
            return "redirect:/login";
        }

        // 5. Generate JWT, set cookie, and redirect to user dashboard
        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        final String jwt = jwtUtil.generateToken(userDetails);
        response.addCookie(createJwtCookie(jwt));

        return "redirect:/dashboard";
    }

    // --- ADMIN LOGIN ---
    @GetMapping("/admin")
    public String showAdminLoginForm() {
        return "admin/login"; // Renders templates/admin/login.html
    }

    @PostMapping("/admin")
    public String loginAdmin(@ModelAttribute LoginRequestDto loginRequest,
                             HttpServletResponse response,
                             RedirectAttributes redirectAttributes) {

        Authentication authentication;
        try {
            // 1. Try to authenticate
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
        } catch (Exception e) {
            // 2. If auth fails, redirect back with an error
            redirectAttributes.addFlashAttribute("error", "Invalid admin username or password");
            return "redirect:/admin";
        }

        // 3. Set authentication in Spring Security's context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4. ROLE CHECK: Only allow ROLE_ADMIN to log in here
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse(null);

        if (!"ROLE_ADMIN".equals(role)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. You do not have admin privileges.");
            return "redirect:/admin";
        }

        // 5. Generate JWT, set cookie, and redirect to admin dashboard
        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        final String jwt = jwtUtil.generateToken(userDetails);
        response.addCookie(createJwtCookie(jwt));

        return "redirect:/admin/dashboard";
    }


    // --- REGISTRATION ---
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register"; // Renders templates/register.html
    }

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

    // --- HELPER METHOD to create the cookie ---
    private Cookie createJwtCookie(String jwt) {
        Cookie jwtCookie = new Cookie("jwt-token", jwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // Set to true in production (HTTPS)
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(10 * 60); // 10 minutes
        return jwtCookie;
    }
}