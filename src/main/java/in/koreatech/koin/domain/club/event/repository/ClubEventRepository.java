package in.koreatech.koin.domain.club.event.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.event.model.ClubEvent;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;
import in.koreatech.koin.global.exception.CustomException;

@JpaRepositoryMarker
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
        WHERE e.notifiedBeforeOneHour = false AND e.startDate >= :now AND e.startDate < :hourLater
        """)
    List<ClubEvent> findAllWithClubUpcomingEventsWithOneHour(LocalDateTime now, LocalDateTime hourLater);

    Optional<ClubEvent> findById(Integer clubId);

    default ClubEvent getById(Integer clubId) {
        return findById(clubId)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_CLUB_EVENT));
    }

    void save(ClubEvent clubEvent);

    Optional<ClubEvent> findByIdAndClubId(Integer id, Integer clubId);

    Optional<List<ClubEvent>> findAllByClubId(Integer clubId);

    default ClubEvent getClubEventByIdAndClubId(Integer id, Integer clubId) {
        return findByIdAndClubId(id, clubId)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_CLUB_EVENT));
    }

    default List<ClubEvent> getAllByClubId(Integer clubId) {
        return findAllByClubId(clubId).orElseThrow(
            () -> CustomException.of(ApiResponseCode.NOT_FOUND_CLUB_EVENT));
    }

    void delete(ClubEvent clubEvent);
}
