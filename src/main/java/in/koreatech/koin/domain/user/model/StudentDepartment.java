package in.koreatech.koin.domain.user.model;

import java.util.Arrays;

import in.koreatech.koin.domain.user.exception.StudentDepartmentNotValidException;
import lombok.Getter;

@Getter
public enum StudentDepartment {
    COMPUTER("컴퓨터공학부"),
    MECHANICAL("기계공학부"),
    MECHATRONICS("메카트로닉스공학부"),
    ELECTRONIC("전기전자통신공학부"),
    DESIGN("디자인공학부"),
    ARCHITECTURAL("건축공학부"),
    CHEMICAL("화학생명공학부"),
    ENERGY("에너지신소재공학부"),
    INDUSTRIAL("산업경영학부"),
    EMPLOYMENT("고용서비스정책학과"),
    ;

    private final String value;

    StudentDepartment(String value) {
        this.value = value;
    }

    public static StudentDepartment from(String value) {
        return Arrays.stream(values())
            .filter(it -> it.value.equals(value))
            .findAny()
            .orElseThrow(() -> StudentDepartmentNotValidException.withDetail("학부(학과) : " + value));
    }
}
