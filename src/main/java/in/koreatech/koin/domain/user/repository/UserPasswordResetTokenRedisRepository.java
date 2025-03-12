package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.model.PasswordResetToken;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;

public interface UserPasswordResetTokenRedisRepository extends Repository<PasswordResetToken, Integer> {

    PasswordResetToken save(PasswordResetToken passwordResetToken);

    Optional<PasswordResetToken> findByResetToken(String resetToken);

    default PasswordResetToken getByResetToken(String resetToken) {
        return findByResetToken(resetToken)
            .orElseThrow(() -> new KoinIllegalArgumentException("Reset token이 존재하지 않습니다.", "resetToken: " + resetToken));
    }

    void deleteById(Integer id);
}
