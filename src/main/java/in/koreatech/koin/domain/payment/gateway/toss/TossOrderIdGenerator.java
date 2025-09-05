package in.koreatech.koin.domain.payment.gateway.toss;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.payment.gateway.pg.PgOrderIdGenerator;

@Component
public class TossOrderIdGenerator implements PgOrderIdGenerator {

    private static final String ORDER_ID_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";
    private static final int ORDER_ID_MIN_LENGTH = 6;
    private static final int ORDER_ID_MAX_LENGTH = 64;
    private final SecureRandom random = new SecureRandom();

    public String generatePgOrderId() {
        int length = ORDER_ID_MIN_LENGTH + random.nextInt(ORDER_ID_MAX_LENGTH - ORDER_ID_MIN_LENGTH + 1);
        StringBuilder sb = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            sb.append(ORDER_ID_CHARACTERS.charAt(random.nextInt(ORDER_ID_CHARACTERS.length())));
        }
        return sb.toString();
    }
}
