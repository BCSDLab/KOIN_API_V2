package in.koreatech.koin.admin.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.User;

public interface AdminUserRepository extends Repository<User, Integer> {

    User save(User user);

    Optional<User> findById(Integer id);

    default User getById(Integer userId) {
        return findById(userId)
            .orElseThrow(() -> UserNotFoundException.withDetail("userId: " + userId));
    }

    boolean existsByNicknameAndIdNot(String nickname, Integer userId);
}
