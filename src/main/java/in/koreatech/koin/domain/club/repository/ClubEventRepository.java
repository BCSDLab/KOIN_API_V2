package in.koreatech.koin.domain.club.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin.domain.club.model.ClubEvent;

public interface ClubEventRepository extends Repository<ClubEvent, Integer> {

    @Query("""
        SELECT e FROM ClubEvent e
        JOIN FETCH e.club
        WHERE e.startDate >= :start AND e.startDate < :end
        """)
    List<ClubEvent> findAllWithClubByStartDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT e FROM ClubEvent e
        JOIN FETCH e.club
        WHERE e.notifiedOneHour = false AND e.startDate >= :now AND e.startDate < :hourLater
        """)
    List<ClubEvent> findAllWithClubUpcomingEventsWithOneHour(LocalDateTime now, LocalDateTime hourLater);

    Optional<ClubEvent> findById(Integer clubId);

    default ClubEvent getById(Integer clubId) {
        return findById(clubId)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_CLUB_EVENT));
    }
}
