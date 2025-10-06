package in.koreatech.koin.domain.community.keyword.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.keyword.model.UserNotificationStatus;
import in.koreatech.koin.config.repository.JpaRepository;

@JpaRepository
public interface UserNotificationStatusRepository extends Repository<UserNotificationStatus, Integer> {

    void save(UserNotificationStatus status);

    Optional<UserNotificationStatus> findByUserId(Integer userId);

    boolean existsByNotifiedArticleIdAndUserId(Integer notifiedArticleId, Integer userId);
}
