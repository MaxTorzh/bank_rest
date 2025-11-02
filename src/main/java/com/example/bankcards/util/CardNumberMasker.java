package com.example.bankcards.util;

import org.springframework.stereotype.Component;

@Component
public class CardNumberMasker {

    public String maskCardNumber(String encryptedCardNumber) {
        return "**** **** **** 1234";
    }
}
