package in.koreatech.koin.domain.user.model;

public record UserDeleteEvent(
    String email,
    UserType userType
) {

}
