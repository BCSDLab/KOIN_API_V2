package in.koreatech.koin.admin.shop.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.shop.model.EventArticle;

public interface AdminEventArticleRepository extends Repository<EventArticle, Integer> {

    @Query("""
        SELECT COUNT(e) > 0 FROM EventArticle e
        WHERE :now BETWEEN e.startDate AND e.endDate
        AND e.shop.id = :shopId
        """)
    boolean isDurationEvent(@Param("shopId") Integer shopId, @Param("now") LocalDate now);
}
