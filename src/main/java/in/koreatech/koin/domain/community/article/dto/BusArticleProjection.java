package in.koreatech.koin.domain.community.article.dto;

import java.time.LocalDateTime;

public interface BusArticleProjection {

    Integer getId();

    String getTitle();

    LocalDateTime getCreatedAt();
}
