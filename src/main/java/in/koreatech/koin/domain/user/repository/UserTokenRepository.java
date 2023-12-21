package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.domain.UserToken;

public interface UserTokenRepository extends Repository<UserToken, Long> {

    UserToken save(UserToken userToken);

    Optional<UserToken> findById(Long userId);
}
