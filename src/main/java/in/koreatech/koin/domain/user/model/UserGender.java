package in.koreatech.koin.domain.user.model;

import java.util.Arrays;

import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.exception.ErrorCode;

public enum UserGender {
    MAN,
    WOMAN,
    ;

    public static UserGender from(Integer index) {
        if (index == null) {
            return null;
        }
        return Arrays.stream(values())
            .filter(it -> it.ordinal() == index)
            .findAny()
            .orElseThrow(() -> CustomException.withDetail(ErrorCode.USER_GENDER_NOT_VALID, "index : " + index));
    }
}
