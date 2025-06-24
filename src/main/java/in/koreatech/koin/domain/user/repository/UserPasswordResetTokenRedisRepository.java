package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.exception.errorcode.ErrorCode;
import in.koreatech.koin.domain.user.model.PasswordResetToken;

public interface UserPasswordResetTokenRedisRepository extends Repository<PasswordResetToken, Integer> {

    PasswordResetToken save(PasswordResetToken passwordResetToken);

    Optional<PasswordResetToken> findByResetToken(String resetToken);

    default PasswordResetToken getByResetToken(String resetToken) {
        return findByResetToken(resetToken)
            .orElseThrow(() -> CustomException.of(ErrorCode.RESET_TOKEN_NOT_VALID, "resetToken: " + resetToken));
    }

    void deleteById(Integer id);
}
