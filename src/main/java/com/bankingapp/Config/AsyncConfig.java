package com.bankingapp.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync // This enables asynchronous methods (@Async)
public class AsyncConfig {
    // This class is intentionally blank.
    // Its only job is to enable @Async.
}