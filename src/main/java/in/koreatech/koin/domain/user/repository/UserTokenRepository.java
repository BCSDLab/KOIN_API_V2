package in.koreatech.koin.domain.user.repository;

import in.koreatech.koin.domain.user.model.UserToken;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface UserTokenRepository extends Repository<UserToken, Long> {

    UserToken save(UserToken userToken);

    Optional<UserToken> findById(Long userId);

    void deleteById(Long id);
}
