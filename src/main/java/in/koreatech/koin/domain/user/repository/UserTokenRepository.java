package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.model.UserToken;

public interface UserTokenRepository extends Repository<UserToken, Long> {

    UserToken save(UserToken userToken);

    Optional<UserToken> findById(Long userId);

    void deleteById(Long id);

    default UserToken getById(Long userId) {
        return findById(userId).orElseThrow(
            () -> new IllegalArgumentException("refresh token이 존재하지 않습니다. userId: " + userId));
    }
}
