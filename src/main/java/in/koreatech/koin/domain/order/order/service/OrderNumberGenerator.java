package in.koreatech.koin.domain.order.order.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

/**
 * 주문 번호를 생성한다.
 * 현재 규칙은 대문자 알파벳과 숫자를 혼합한 10자리이며, 리셋 없이 전역으로 유일하다.
 * 규칙이 변경될 수 있으므로 생성 책임을 이 컴포넌트로 격리한다.
 */
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
