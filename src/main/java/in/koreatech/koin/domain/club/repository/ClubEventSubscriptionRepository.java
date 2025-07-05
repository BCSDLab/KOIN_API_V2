package in.koreatech.koin.domain.club.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.model.ClubEventSubscription;

public interface ClubEventSubscriptionRepository extends Repository<ClubEventSubscription, Integer> {

    @Query("SELECT s from ClubEventSubscription s JOIN FETCH s.user WHERE s.clubEvent.id IN :eventIds")
    List<ClubEventSubscription> findAllWithUserByEventIdIn(List<Integer> eventIds);

    boolean existsByClubEventIdAndUserId(Integer eventId, Integer userId);

    void deleteByClubEventIdAndUserId(Integer eventId, Integer userId);

    void save(ClubEventSubscription subscription);
}
