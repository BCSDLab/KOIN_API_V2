package in.koreatech.koin.domain.student.model;

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
    APPLIED_CHEMICAL("응용화학공학부"),
    ;

    private final String value;

    StudentDepartment(String value) {
        this.value = value;
    }

    public static boolean isValid(String department) {
        for (StudentDepartment value : StudentDepartment.values()) {
            if (value.getValue().equals(department)) {
                return true;
            }
        }
        return false;
    }
}
