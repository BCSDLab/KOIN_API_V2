package in.koreatech.koin.domain.user.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;

public interface UserRepository extends Repository<User, Integer> {

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndUserType(String email, UserType userType);

    Optional<User> findByPhoneNumberAndUserType(String phoneNumber, UserType userType);

    Optional<User> findById(Integer id);

    Optional<User> findByNickname(String nickname);

    Optional<User> findAllByResetToken(String resetToken);

    default User getByEmail(String email) {
        return findByEmail(email)
            .orElseThrow(() -> UserNotFoundException.withDetail("account: " + email));
    }

    default User getByPhoneNumber(String phoneNumber, UserType userType) {
        return findByPhoneNumberAndUserType(phoneNumber, userType)
            .orElseThrow(() -> UserNotFoundException.withDetail("account: " + phoneNumber));
    }

    default User getById(Integer userId) {
        return findById(userId)
            .orElseThrow(() -> UserNotFoundException.withDetail("userId: " + userId));
    }

    default User getById(String id, UserType userType) {
        return findByEmailAndUserType(id, userType)
            .orElseThrow(() -> UserNotFoundException.withDetail("id: " + id));
    }

    default User getByResetToken(String resetToken) {
        return findAllByResetToken(resetToken)
            .orElseThrow(() -> UserNotFoundException.withDetail("resetToken: " + resetToken));
    }

    boolean existsByNickname(String nickname);

    void delete(User user);

    List<User> findAllByName(String name);

    List<User> findAllByIdIn(List<Integer> ids);

    default Map<Integer, User> findAllByIdInMap(List<Integer> ids) {
        return findAllByIdIn(ids).stream()
            .collect(Collectors.toMap(User::getId, user -> user));
    }
}
