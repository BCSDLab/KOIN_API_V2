package in.koreatech.koin.domain.chat.model;

import java.util.List;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatDomainType {
    LOST_ITEM("lost-item", List.of(UserType.GENERAL, UserType.STUDENT, UserType.COUNCIL)),
    CALLVAN("callvan", List.of(UserType.STUDENT)),
    TEAM_RECRUITMENT("team-recruitment", List.of(UserType.STUDENT));

    private final String prefix;
    private final List<UserType> permittedUserTypes;

    public boolean supports(User user) {
        return permittedUserTypes.contains(user.getUserType());
    }

    public void authorize(User user) {
        user.authorizeAndGetId(permittedUserTypes.toArray(UserType[]::new));
    }
}
