package in.koreatech.koin.domain.community.article.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LostItemArticlesRequest(
    List<LostItemArticleRequest> articles
) {

}
