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
    SEMICONDUCTOR_DISPLAY_ENGINEERING("반도체·디스플레이공학과"),
    FUTURE_CONVERGENCE("미래융합학부"),
    ENGINEERING_CONVERGENCE_AUTONOMOUS_MAJOR("공학융합자율전공"),
    ICT_CONVERGENCE_AUTONOMOUS_MAJOR("ICT융합자율전공"),
    SOCIAL_CONVERGENCE_AUTONOMOUS_MAJOR("사회융합자율전공"),
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
