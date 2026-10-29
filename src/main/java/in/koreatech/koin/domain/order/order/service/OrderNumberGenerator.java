package in.koreatech.koin.domain.order.order.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class OrderNumberGenerator {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LENGTH = 10;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder orderNumber = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            orderNumber.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return orderNumber.toString();
    }
}
