package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.domain.user.model.RefreshToken;

public interface RefreshTokenRedisRepository extends Repository<RefreshToken, String> {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findById(String id);

    default RefreshToken getById(String id) {
        return findById(id)
            .orElseThrow(() -> new KoinIllegalArgumentException("refresh token이 존재하지 않습니다.", "id: " + id));
    }

    void deleteById(String id);
}
