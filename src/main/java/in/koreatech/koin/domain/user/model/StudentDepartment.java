package in.koreatech.koin.domain.user.model;

import lombok.Getter;

@Getter
public enum StudentDepartment {
    컴퓨터공학부,
    기계공학부,
    메카트로닉스공학부,
    전기전자통신공학부,
    디자인공학부,
    건축공학부,
    화학생명공학부,
    에너지신소재공학부,
    산업경영학부,
    고용서비스정책학과,
    ;

    public static boolean isValid(String department) {
        for (StudentDepartment value : StudentDepartment.values()) {
            if (value.name().equals(department)) {
                return true;
            }
        }
        return false;
    }
}
