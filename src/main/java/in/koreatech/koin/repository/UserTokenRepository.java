package in.koreatech.koin.repository;

import in.koreatech.koin.domain.user.UserToken;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface UserTokenRepository extends Repository<UserToken, Long> {

    UserToken save(UserToken userToken);

    Optional<UserToken> findById(Long userId);
}
