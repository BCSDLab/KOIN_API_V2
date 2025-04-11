package in.koreatech.koin.admin.user.enums;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import lombok.Getter;

@Getter
public enum Role {
    TRACK_LEADER("트랙장", TRUE),
    TRACK_REGULAR("레귤러", FALSE),
    BCSD_PRESIDENT("BCSD 회장", TRUE),
    BCSD_VICE_PRESIDENT("BCSD 부회장", TRUE),
    ;

    private final String name;
    private final Boolean canCreateAdmin;

    Role(String name, Boolean canCreateAdmin) {
        this.name = name;
        this.canCreateAdmin = canCreateAdmin;
    }
}
