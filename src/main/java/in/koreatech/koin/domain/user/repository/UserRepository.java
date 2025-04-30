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

    Optional<User> findByEmailAndUserTypeIn(String email, List<UserType> userTypes);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByPhoneNumberAndUserType(String phoneNumber, UserType userType);

    Optional<User> findByPhoneNumberAndUserTypeIn(String phoneNumber, List<UserType> userTypes);

    Optional<User> findById(Integer id);

    Optional<User> findByUserIdAndUserTypeIn(String userId, List<UserType> userTypes);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByUserId(String userId);

    default User getByEmail(String email) {
        return findByEmail(email)
            .orElseThrow(() -> UserNotFoundException.withDetail("account: " + email));
    }

    default User getByPhoneNumberAndUserType(String phoneNumber, UserType userType) {
        return findByPhoneNumberAndUserType(phoneNumber, userType)
            .orElseThrow(() -> UserNotFoundException.withDetail("account: " + phoneNumber));
    }

    default User getByPhoneNumber(String phoneNumber) {
        return findByPhoneNumber(phoneNumber)
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

    default User getByUserId(String loginId) {
        return findByUserId(loginId)
            .orElseThrow(() -> UserNotFoundException.withDetail("loginId: " + loginId));
    }

    boolean existsByNickname(String nickname);

    boolean existsByUserId(String userId);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    void delete(User user);

    List<User> findAllByName(String name);

    List<User> findAllByIdIn(List<Integer> ids);

    default Map<Integer, User> findAllByIdInMap(List<Integer> ids) {
        return findAllByIdIn(ids).stream()
            .collect(Collectors.toMap(User::getId, user -> user));
    }

    default User getByEmailAndUserTypeIn(String email, List<UserType> userTypes) {
        return findByEmailAndUserTypeIn(email, userTypes)
            .orElseThrow(() -> UserNotFoundException.withDetail("email: " + email));
    }

    default User getByPhoneNumberAndUserTypeIn(String phoneNumber, List<UserType> userTypes) {
        return findByPhoneNumberAndUserTypeIn(phoneNumber, userTypes)
            .orElseThrow(() -> UserNotFoundException.withDetail("phoneNumber: " + phoneNumber));
    }

    default User getByUserIdAndUserTypeIn(String loginId, List<UserType> userTypes) {
        return findByUserIdAndUserTypeIn(loginId, userTypes)
            .orElseThrow(() -> UserNotFoundException.withDetail("loginId: " + loginId));
    }
}
