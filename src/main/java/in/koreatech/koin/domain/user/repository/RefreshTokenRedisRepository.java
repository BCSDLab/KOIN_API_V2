package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.exception.errorcode.ErrorCode;
import in.koreatech.koin.domain.user.model.RefreshToken;

public interface RefreshTokenRedisRepository extends Repository<RefreshToken, String> {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findById(String id);

    default RefreshToken getById(String id) {
        return findById(id)
            .orElseThrow(() -> CustomException.of(ErrorCode.REFRESH_TOKEN_NOT_FOUND, "id: " + id));
    }

    void deleteById(String id);
}
