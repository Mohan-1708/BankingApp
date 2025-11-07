package com.bankingapp.Controller;

import com.bankingapp.Config.JwtUtil;
import com.bankingapp.Dto.LoginRequestDto;
import com.bankingapp.Dto.OtpVerificationDto;
import com.bankingapp.Dto.UserRegistrationDto;
import com.bankingapp.Service.OtpService;
import com.bankingapp.Service.UserService;
import com.bankingapp.Service.UserServiceImpl;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserServiceImpl userDetailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    // --- STEP 1: REGISTRATION ---
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserRegistrationDto registrationDto,
                               RedirectAttributes redirectAttributes) {
        try {
            otpService.generateAndSendRegistrationOtp(registrationDto);
            // We send the email and type to the GET handler
            redirectAttributes.addFlashAttribute("email", registrationDto.getEmail());
            redirectAttributes.addFlashAttribute("type", "register");
            return "redirect:/verify-otp";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    // --- STEP 2: LOGIN (Both User and Admin) ---
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/admin")
    public String showAdminLoginForm() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute LoginRequestDto loginRequest,
                            RedirectAttributes redirectAttributes) {
        return handleLogin(loginRequest, "redirect:/login", redirectAttributes);
    }

    @PostMapping("/admin")
    public String loginAdmin(@ModelAttribute LoginRequestDto loginRequest,
                             RedirectAttributes redirectAttributes) {
        return handleLogin(loginRequest, "redirect:/admin", redirectAttributes);
    }

    private String handleLogin(LoginRequestDto loginRequest, String failureRedirect, RedirectAttributes redirectAttributes) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            otpService.generateAndSendLoginOtp(loginRequest.getEmail());

            // We send the email and type to the GET handler
            redirectAttributes.addFlashAttribute("email", loginRequest.getEmail());
            redirectAttributes.addFlashAttribute("type", "login");
            return "redirect:/verify-otp";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return failureRedirect;
        }
    }


    // --- STEP 3: OTP VERIFICATION (UPDATED) ---
    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(Model model,
                                    @ModelAttribute("email") String email,
                                    @ModelAttribute("type") String type) {

        // 1. Check if the flash attributes exist. If not, redirect to login.
        if (email == null || email.isEmpty() || type == null || type.isEmpty()) {
            return "redirect:/login";
        }

        // 2. *** THIS IS THE FIX ***
        // Create the DTO, set the values from the flash attributes,
        // and add that *DTO* to the model for the form to use.
        OtpVerificationDto dto = new OtpVerificationDto();
        dto.setEmail(email);
        dto.setType(type);

        model.addAttribute("otpVerificationDto", dto);
        model.addAttribute("email", email); // Keep this for the display text

        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@ModelAttribute("otpVerificationDto") OtpVerificationDto verificationDto, // <-- Bind to the object
                            HttpServletResponse response,
                            RedirectAttributes redirectAttributes) {

        String email = verificationDto.getEmail();
        String otp = verificationDto.getOtp();
        String type = verificationDto.getType();

        // 1. Validate the OTP
        if (!otpService.validateOtp(email, otp)) {
            // If validation fails, we MUST send the email and type back
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("type", type);
            redirectAttributes.addFlashAttribute("error", "Invalid or expired OTP. Please try again.");
            return "redirect:/verify-otp";
        }

        // 2. If OTP is valid, proceed based on type
        if ("register".equals(type)) {
            // --- This is a NEW REGISTRATION ---
            try {
                UserRegistrationDto dto = otpService.getPendingRegistration(email);
                if (dto == null) {
                    redirectAttributes.addFlashAttribute("error", "Registration session expired. Please register again.");
                    return "redirect:/register";
                }
                userService.registerUser(dto.getName(), dto.getEmail(), dto.getPassword());
                redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
                return "redirect:/login";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error creating account: " + e.getMessage());
                return "redirect:/register";
            }

        } else if ("login".equals(type)) {
            // --- This is a LOGIN ---
            UserDetails userDetails = userDetailService.loadUserByUsername(email);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            final String jwt = jwtUtil.generateToken(userDetails);
            response.addCookie(createJwtCookie(jwt));

            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst().orElse(null);

            if ("ROLE_ADMIN".equals(role)) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/dashboard";
            }
        }

        redirectAttributes.addFlashAttribute("error", "An unknown error occurred.");
        return "redirect:/login";
    }

    // --- HELPER METHOD (Unchanged) ---
    private Cookie createJwtCookie(String jwt) {
        Cookie jwtCookie = new Cookie("jwt-token", jwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // Set to true in production (HTTPS)
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(10 * 60); // 10 minutes
        return jwtCookie;
    }
}