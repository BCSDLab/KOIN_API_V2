package in.koreatech.koin.domain.community.util;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.model.HotArticle;
import in.koreatech.koin.domain.community.repository.HotArticleRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidationScheduler {

    private final HotArticleRepository hotArticleRepository;

    @Scheduled(cron = "0 0 * * * *") /* every hour */
    public void validateHits() {
        List<HotArticle> hotArticles = hotArticleRepository.findAll();
        hotArticles.forEach(HotArticle::validate);
        hotArticles.forEach(hotArticleRepository::save);
        hotArticles.stream()
            .filter(HotArticle::isEmpty)
            .map(HotArticle::getId)
            .forEach(hotArticleRepository::deleteById);
    }
}
