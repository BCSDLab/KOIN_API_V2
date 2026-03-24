package in.koreatech.koin.domain.community.article.dto;

import java.time.LocalDateTime;

public record BusArticleProjection(
    Integer id,
    String title,
    LocalDateTime createdAt
) {

}
