package in.koreatech.koin.domain.user.model;

import java.util.Arrays;

public enum UserGender {
    MAN,
    WOMAN,
    ;

    public static UserGender from(Integer index) {
        if (index != null) {
            return Arrays.stream(values())
                .filter(it -> it.ordinal() == index)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("잘못된 성별 인덱스 입니다. index : " + index));
        }
        return null;
    }
}
