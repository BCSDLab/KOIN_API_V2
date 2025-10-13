package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.model.RefreshToken;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

public interface RefreshTokenRedisRepository extends Repository<RefreshToken, String> {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findById(String id);

    default RefreshToken getById(String id) {
        return findById(id)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_REFRESH_TOKEN, "id: " + id));
    }

    void deleteById(String id);
}
