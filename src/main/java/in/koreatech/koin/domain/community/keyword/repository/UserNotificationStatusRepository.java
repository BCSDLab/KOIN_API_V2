package in.koreatech.koin.domain.community.keyword.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.keyword.model.UserNotificationStatus;

public interface UserNotificationStatusRepository extends Repository<UserNotificationStatus, Integer> {

    void save(UserNotificationStatus status);

    boolean existsByLastNotifiedArticleIdAndUserId(Integer lastNotificationArticleId, Integer userId);
}
