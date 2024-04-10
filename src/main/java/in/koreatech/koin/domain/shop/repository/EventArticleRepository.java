package in.koreatech.koin.domain.shop.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.ownershop.exception.EventArticleNotFoundException;
import in.koreatech.koin.domain.shop.model.EventArticle;

public interface EventArticleRepository extends Repository<EventArticle, Integer> {

    EventArticle save(EventArticle eventArticle);

    List<EventArticle> findAllByShopId(Integer shopId);

    @Query("""
        SELECT COUNT(e) > 0 FROM EventArticle e
        WHERE :now BETWEEN e.startDate AND e.endDate
        AND e.shop.id = :shopId
        """)
    Boolean isEvent(@Param("shopId") Integer shopId, @Param("now") LocalDate now);

    Optional<EventArticle> findById(Integer id);

    default EventArticle getById(Integer eventId) {
        return findById(eventId).orElseThrow(() -> EventArticleNotFoundException.withDetail("eventId: " + eventId));
    }

    void deleteById(Integer eventId);

    @Query("SELECT e FROM EventArticle e WHERE e.startDate <= CURRENT_DATE AND e.endDate >= CURRENT_DATE AND e.isDeleted = false ORDER BY e.endDate ASC")
    List<EventArticle> findAllDurationEvents();
}
