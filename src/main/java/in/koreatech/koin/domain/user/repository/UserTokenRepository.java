package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.model.UserToken;

public interface UserTokenRepository extends Repository<UserToken, Integer> {

    UserToken save(UserToken userToken);

    Optional<UserToken> findById(Integer userId);

    void deleteById(Integer id);

    default UserToken getById(Integer userId) {
        return findById(userId).orElseThrow(
            () -> new IllegalArgumentException("refresh token이 존재하지 않습니다. userId: " + userId));
    }
}
