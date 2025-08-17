package in.koreatech.koin.domain.user.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;

public interface UserRepository extends Repository<User, Integer> {

    // Create
    User save(User user);

    // Read
    Optional<User> findById(Integer id);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndUserTypeIn(String email, List<UserType> userTypes);

    Optional<User> findByPhoneNumberAndUserType(String phoneNumber, UserType userType);

    Optional<User> findByPhoneNumberAndUserTypeIn(String phoneNumber, List<UserType> userTypes);

    Optional<User> findByLoginIdAndUserTypeIn(String loginId, List<UserType> userTypes);

    Optional<User> findByNickname(String nickname);

    List<User> findAllByName(String name);

    List<User> findAllByIdIn(List<Integer> ids);

    boolean existsByNicknameAndUserTypeIn(String nickname, List<UserType> userTypes);

    boolean existsByLoginIdAndUserTypeIn(String loginId, List<UserType> userTypes);

    boolean existsByPhoneNumberAndUserTypeIn(String phoneNumber, List<UserType> userTypes);

    boolean existsByEmailAndUserTypeIn(String email, List<UserType> userTypes);

    boolean existsById(Integer id);

    void delete(User user);

    default User getById(Integer userId) {
        return findById(userId)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_USER, "userId: " + userId));
    }

    default User getByEmailAndUserTypeIn(String email, List<UserType> userTypes) {
        return findByEmailAndUserTypeIn(email, userTypes)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_USER, "email: " + email));
    }

    default User getByPhoneNumberAndUserType(String phoneNumber, UserType userType) {
        return findByPhoneNumberAndUserType(phoneNumber, userType)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_USER, "account: " + phoneNumber));
    }

    default User getByPhoneNumberAndUserTypeIn(String phoneNumber, List<UserType> userTypes) {
        return findByPhoneNumberAndUserTypeIn(phoneNumber, userTypes)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_USER, "account: " + phoneNumber));
    }

    default User getByLoginIdAndUserTypeIn(String loginId, List<UserType> userTypes) {
        return findByLoginIdAndUserTypeIn(loginId, userTypes)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_USER, "loginId: " + loginId));
    }

    default Map<Integer, User> getAllByIdInMap(List<Integer> ids) {
        return findAllByIdIn(ids).stream()
            .collect(Collectors.toMap(User::getId, user -> user));
    }
}
