package in.koreatech.koin.domain.shop.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.EventArticle;

public interface EventArticleRepository extends Repository<EventArticle, Long> {

    EventArticle save(EventArticle eventArticle);

    List<EventArticle> findAllByShopId(Long shopId);

}
