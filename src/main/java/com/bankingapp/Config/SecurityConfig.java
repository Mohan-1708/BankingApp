package com.bankingapp.Config;

import com.bankingapp.Service.UserServiceImpl;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest; // <-- IMPORT THIS
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // --- THIS IS THE FIX ---
                        // 1. Permit all static resources (CSS, JS, images, etc.)
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        // 2. Permit our public pages
                        .requestMatchers("/login", "/register").permitAll()
                        // 3. All other requests must be authenticated
                        .anyRequest().authenticated()
                )
                // Configure session management to be STATELESS (we use JWTs, not sessions)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Add our custom authentication provider
                .authenticationProvider(authenticationProvider())

                // Add our JWT filter BEFORE the default Spring Security filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // --- EXPLICITLY DISABLE FORMLOGIN ---
                .formLogin(AbstractHttpConfigurer::disable)

                // --- ADD THIS EXCEPTION HANDLING BLOCK ---
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )

                // Configure logout (this is still correct)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            // Clear the JWT cookie on logout
                            Cookie cookie = new Cookie("jwt-token", null);
                            cookie.setPath("/");
                            cookie.setHttpOnly(true);
                            cookie.setMaxAge(0); // Expire the cookie
                            response.addCookie(cookie);
                            // Redirect to login page
                            response.sendRedirect("/login?logout");
                        })
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // Tell the provider where to get user details
        authProvider.setUserDetailsService(userDetailsService);
        // Tell the provider what password encoder to use
        // --- FIX: Use the injected field 'passwordEncoder', not the method 'passwordEncoder()' ---
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // Expose the AuthenticationManager as a bean
        return config.getAuthenticationManager();
    }
}

