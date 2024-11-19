package in.koreatech.koin.domain.shop.cache.dto;

import in.koreatech.koin.domain.shop.model.event.EventArticle;
import in.koreatech.koin.domain.shop.model.event.EventArticleImage;
import java.util.List;

public record EventArticleCache(
        String title,
        List<String> thumbnailImages
) {
    
    public static EventArticleCache from(EventArticle eventArticle) {
        return new EventArticleCache(
                eventArticle.getTitle(),
                eventArticle.getThumbnailImages().stream()
                        .map(EventArticleImage::getThumbnailImage)
                        .toList()
        );
    }
}
