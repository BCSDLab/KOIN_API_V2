package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.global.marker.RedisRepositoryMarker;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.domain.user.model.PasswordResetToken;

@RedisRepositoryMarker
public interface UserPasswordResetTokenRedisRepository extends Repository<PasswordResetToken, Integer> {

    PasswordResetToken save(PasswordResetToken passwordResetToken);

    Optional<PasswordResetToken> findByResetToken(String resetToken);

    default PasswordResetToken getByResetToken(String resetToken) {
        return findByResetToken(resetToken)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_RESET_TOKEN, "resetToken: " + resetToken));
    }

    void deleteById(Integer id);
}
