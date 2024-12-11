package in.koreatech.koin.domain.shop.repository.event;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.event.EventArticleImage;

public interface EventArticleImageRepository extends Repository<EventArticleImage, Integer> {

    void save(EventArticleImage eventArticleImage);
}
