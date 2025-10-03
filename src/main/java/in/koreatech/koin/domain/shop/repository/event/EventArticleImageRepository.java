package in.koreatech.koin.domain.shop.repository.event;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.event.EventArticleImage;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface EventArticleImageRepository extends Repository<EventArticleImage, Integer> {

    void save(EventArticleImage eventArticleImage);
}
