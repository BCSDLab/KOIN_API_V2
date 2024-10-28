package in.koreatech.koin.admin.user.enums;

import java.util.Arrays;

import in.koreatech.koin.admin.user.exception.AdminTeamNotValidException;
import lombok.Getter;

@Getter
public enum TeamType {
    BUSINESS("비즈니스"),
    CAMPUS("캠퍼스"),
    USER("유저");

    private final String value;

    TeamType(String value) {
        this.value = value;
    }

    public static void checkTeamValid(String teamName) {
        boolean isValid = Arrays.stream(TeamType.values())
            .anyMatch(teamType -> teamType.getValue().equals(teamName));

        if (!isValid) {
            throw AdminTeamNotValidException.withDetail("teamName : " + teamName);
        }
    }
}
