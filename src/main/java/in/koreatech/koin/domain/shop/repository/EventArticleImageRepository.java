package in.koreatech.koin.domain.shop.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.article.EventArticleImage;

public interface EventArticleImageRepository extends Repository<EventArticleImage, Integer> {

    void save(EventArticleImage eventArticleImage);
}
