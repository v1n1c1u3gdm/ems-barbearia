package com.emsbarbearia.service;

public interface OtpSender {

    void send(String phone, String code);
}
