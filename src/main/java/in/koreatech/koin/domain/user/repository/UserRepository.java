package in.koreatech.koin.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.exception.errorcode.ErrorCode;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;

public interface UserRepository extends Repository<User, Integer> {

    User save(User user);

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

    void delete(User user);

    default User getById(Integer userId) {
        return findById(userId)
            .orElseThrow(() -> CustomException.of(ErrorCode.USER_NOT_FOUND, "userId: " + userId));
    }

    default User getByLoginIdAndUserTypeIn(String loginId, List<UserType> userTypes) {
        return findByLoginIdAndUserTypeIn(loginId, userTypes)
            .orElseThrow(() -> CustomException.of(ErrorCode.USER_NOT_FOUND, "loginId: " + loginId));
    }
}
