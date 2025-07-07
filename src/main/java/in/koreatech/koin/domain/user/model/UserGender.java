package in.koreatech.koin.domain.user.model;

import java.util.Arrays;

import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.code.ApiResponseCode;

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
            .orElseThrow(() -> CustomException.of(ApiResponseCode.INVALID_GENDER_INDEX, "index : " + index));
    }
}
