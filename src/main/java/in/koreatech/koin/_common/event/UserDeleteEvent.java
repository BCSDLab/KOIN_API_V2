package in.koreatech.koin._common.event;

import in.koreatech.koin.domain.user.model.UserType;

public record UserDeleteEvent(
    String email,
    UserType userType
) {

}
