package com.emsbarbearia.config;

import com.emsbarbearia.service.LoggingOtpSender;
import com.emsbarbearia.service.OtpSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OtpConfig {

    @Bean
    public OtpSender otpSender() {
        return new LoggingOtpSender();
    }
}
