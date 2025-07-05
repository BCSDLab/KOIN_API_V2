package in.koreatech.koin.domain.club.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

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
}
