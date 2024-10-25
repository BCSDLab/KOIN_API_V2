package in.koreatech.koin.admin.user.dto;

import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.user.model.User;

public record AdminUserResponse(
    String email,
    String name,
    String trackName,
    String teamName
) {
    public static AdminUserResponse of(Admin admin) {
        User user = admin.getUser();

        return new AdminUserResponse(
            user.getEmail(),
            user.getName(),
            admin.getTrackName(),
            admin.getTeamName()
        );
    }
}
