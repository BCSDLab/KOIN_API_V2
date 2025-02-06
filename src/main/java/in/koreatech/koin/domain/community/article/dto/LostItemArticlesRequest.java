package in.koreatech.koin.domain.community.article.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.Valid;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LostItemArticlesRequest(
    @Valid
    List<LostItemArticleRequest> articles
) {

}
