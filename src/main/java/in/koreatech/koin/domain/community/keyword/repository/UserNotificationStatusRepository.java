package in.koreatech.koin.domain.community.keyword.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.keyword.model.UserNotificationStatus;

public interface UserNotificationStatusRepository extends Repository<UserNotificationStatus, Integer> {

    @Query("SELECT u.lastNotifiedArticleId FROM UserNotificationStatus u WHERE u.userId = :userId")
    Optional<Integer> findLastNotifiedArticleIdByUserId(Integer userId);

    Optional<UserNotificationStatus> findByUserId(Integer userId);

    void save(UserNotificationStatus status);
}
