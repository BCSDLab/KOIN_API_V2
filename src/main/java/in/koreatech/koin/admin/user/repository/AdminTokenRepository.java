package in.koreatech.koin.admin.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public interface AdminTokenRepository extends Repository<UserToken, Integer> {

    UserToken save(UserToken userToken);

    Optional<UserToken> findById(Integer userId);

    void deleteById(Integer id);

    default UserToken getById(Integer userId) {
        return findById(userId)
            .orElseThrow(() -> new KoinIllegalArgumentException("refresh token이 존재하지 않습니다. ", "userId: " + userId));
    }
}
