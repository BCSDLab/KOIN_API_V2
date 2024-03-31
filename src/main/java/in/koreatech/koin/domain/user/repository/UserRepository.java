package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.User;

public interface UserRepository extends Repository<User, Long> {

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByAuthToken(String authToken);

    default User getByEmail(String email) {
        return findByEmail(email)
            .orElseThrow(() -> UserNotFoundException.withDetail("email: " + email));
    }

    default User getById(Long userId) {
        return findById(userId)
            .orElseThrow(() -> UserNotFoundException.withDetail("userId: " + userId));
    }

    default User getByNickname(String nickname) {
        return findByNickname(nickname)
            .orElseThrow(() -> UserNotFoundException.withDetail("nickname: " + nickname));
    }

    boolean existsByNickname(String nickname);

    void delete(User user);
}
