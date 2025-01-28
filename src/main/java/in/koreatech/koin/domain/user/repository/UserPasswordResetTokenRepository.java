package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.model.PasswordResetToken;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;

public interface UserPasswordResetTokenRepository extends Repository<PasswordResetToken, Integer> {

    PasswordResetToken save(PasswordResetToken passwordResetToken);

    Optional<PasswordResetToken> findById(Integer userId);

    default PasswordResetToken getById(Integer userId) {
        return findById(userId)
            .orElseThrow(() -> new KoinIllegalArgumentException("Reset token이 존재하지 않습니다.", "userId: " + userId));
    }

    void deleteById(Integer id);
}
