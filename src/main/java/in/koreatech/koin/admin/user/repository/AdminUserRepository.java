package in.koreatech.koin.admin.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;

public interface AdminUserRepository extends Repository<User, Integer> {

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Integer id);

    Optional<User> findByLoginId(String loginId);

    @Query("""
        SELECT COUNT(u) FROM User u
        WHERE u.userType = :userType
        AND u.isAuthed = :isAuthed
        """)
    Integer findUsersCountByUserTypeAndIsAuthed(
            @Param("userType") UserType userType,
            @Param("isAuthed") Boolean isAuthed);

    default User getByEmail(String email) {
        return findByEmail(email)
            .orElseThrow(() -> UserNotFoundException.withDetail("account: " + email));
    }

    void delete(User user);

    default User getById(Integer userId) {
        return findById(userId)
            .orElseThrow(() -> UserNotFoundException.withDetail("userId: " + userId));
    }

    default User getByLoginId(String loginId) {
        return findByLoginId(loginId)
            .orElseThrow(() -> UserNotFoundException.withDetail("loginId: " + loginId));
    }

    boolean existsByNicknameAndIdNot(String nickname, Integer userId);
}
