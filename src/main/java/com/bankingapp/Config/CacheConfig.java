package com.bankingapp.Config;

import com.bankingapp.Dto.UserRegistrationDto;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    /**
     * Creates a singleton cache for storing OTPs.
     * Key: email, Value: OTP
     */
    @Bean(name = "otpCache")
    public Cache<String, String> otpCache() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES) // OTPs expire after 5 minutes
                .build();
    }

    /**
     * Creates a singleton cache for storing pending user registrations.
     * Key: email, Value: UserRegistrationDto
     */
    @Bean(name = "registrationCache")
    public Cache<String, UserRegistrationDto> registrationCache() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES) // Data expires after 5 minutes
                .build();
    }
}