package com.emsbarbearia.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingOtpSender implements OtpSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingOtpSender.class);

    @Override
    public void send(String phone, String code) {
        log.info("OTP for {} (dev/stub): {}", phone, code);
    }
}
