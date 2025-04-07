package in.koreatech.koin.domain.user.model;

import lombok.Getter;

/**
 * 인증 유형을 나타내는 enum
 */
@Getter
public enum VerificationType {
    EMAIL("email"),
    SMS("sms");

    private final String value;

    VerificationType(String value) {
        this.value = value;
    }
}
