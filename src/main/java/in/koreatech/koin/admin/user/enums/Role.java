package in.koreatech.koin.admin.user.enums;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import lombok.Getter;

@Getter
public enum Role {
    TRACK_LEADER(TRUE),
    TRACK_REGULAR(FALSE),
    BCSD_PRESIDENT(TRUE),
    BCSD_VICE_PRESIDENT(TRUE),
    ;

    private final Boolean canCreateAdmin;

    Role(Boolean canCreateAdmin) {
        this.canCreateAdmin = canCreateAdmin;
    }
}
