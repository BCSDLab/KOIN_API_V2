package in.koreatech.koin.domain.shop.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.ownershop.exception.EventArticleNotFoundException;
import in.koreatech.koin.domain.shop.model.EventArticle;

public interface EventArticleRepository extends Repository<EventArticle, Long> {

    EventArticle save(EventArticle eventArticle);

    List<EventArticle> findAllByShopId(Long shopId);

    @Query("""
        SELECT COUNT(e) > 0 FROM EventArticle e
        WHERE :now BETWEEN e.startDate AND e.endDate
        AND e.shop.id = :shopId
        """)
    Boolean isEvent(@Param("shopId") Long shopId, @Param("now") LocalDate now);

    Optional<EventArticle> findById(Long id);

    default EventArticle getById(Long eventId) {
        return findById(eventId).orElseThrow(() -> EventArticleNotFoundException.withDetail("eventId: " + eventId));
    }

    void deleteById(Long eventId);
}
