package com.wexp.shared.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PublicIdGenerator {
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(PublicIdType type) {
        StringBuilder id = new StringBuilder(16);
        id.append(type.getPrefix()).append('_');

        for (int i = 0; i < 12; i++) {
            id.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }

        return id.toString();
    }
}
