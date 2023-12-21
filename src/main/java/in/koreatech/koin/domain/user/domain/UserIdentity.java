package in.koreatech.koin.domain.user.domain;

import lombok.Getter;

/**
 * 신원 (0: 학생, 1: 대학원생, 2: 교수, 3: 교직원, 4: 졸업생, 5: 점주)
 */
@Getter
public enum UserIdentity {
    UNDERGRADUATE("학부생"),
    GRADUATE("대학원생"),
    PROFESSOR("교수"),
    STAFF("교직원"),
    ALUMNI("졸업생"),
    OWNER("점주"),
    ;

    private final String value;

    UserIdentity(String value) {
        this.value = value;
    }
}
