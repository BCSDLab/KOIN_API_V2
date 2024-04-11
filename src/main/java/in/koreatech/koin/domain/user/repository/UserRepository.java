package in.koreatech.koin.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.User;

public interface UserRepository extends Repository<User, Integer> {

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Integer id);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByAuthToken(String authToken);

    Optional<User> findAllByResetToken(String resetToken);

    default User getByEmail(String email) {
        return findByEmail(email)
            .orElseThrow(() -> UserNotFoundException.withDetail("email: " + email));
    }

    default User getById(Integer userId) {
        return findById(userId)
            .orElseThrow(() -> UserNotFoundException.withDetail("userId: " + userId));
    }

    default User getByNickname(String nickname) {
        return findByNickname(nickname)
            .orElseThrow(() -> UserNotFoundException.withDetail("nickname: " + nickname));
    }

    default User getByResetToken(String resetToken) {
        return findAllByResetToken(resetToken)
            .orElseThrow(() -> UserNotFoundException.withDetail("resetToken: " + resetToken));
    }

    boolean existsByNickname(String nickname);

    void delete(User user);

    List<User> findAllByDeviceTokenIsNotNull();
}
