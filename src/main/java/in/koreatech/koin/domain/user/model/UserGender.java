package in.koreatech.koin.domain.user.model;

import java.util.Arrays;

import in.koreatech.koin.domain.user.exception.UserGenderNotValidException;

public enum UserGender {
    MAN,
    WOMAN,
    ;

    public static UserGender from(Integer index) {
        return Arrays.stream(values())
            .filter(it -> it.ordinal() == index)
            .findAny()
            .orElseThrow(() -> UserGenderNotValidException.withDetail("잘못된 성별 인덱스 입니다. index : " + index));
    }
}
