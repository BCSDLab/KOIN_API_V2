package in.koreatech.koin.domain.community.keyword.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import io.swagger.v3.oas.annotations.media.Schema;

public record ArticleKeywordsResponse (

    @Schema(description = "키워드 수", example = "3")
    Integer count,

    @Schema(description = "키워드 목록")
    @JsonProperty("keywords")
    List<InnerKeywordResponse> innerKeywordResponseList
) {

    public static ArticleKeywordsResponse from(List<ArticleKeywordUserMap> articleKeywordUserMaps) {
        if (articleKeywordUserMaps.isEmpty()) {
            return new ArticleKeywordsResponse(0, List.of());
        }

        List<InnerKeywordResponse> keywords = articleKeywordUserMaps.stream()
            .map(userMap -> new InnerKeywordResponse(
                userMap.getArticleKeyword().getId(),
                userMap.getArticleKeyword().getKeyword()))
            .collect(Collectors.toList());

        return new ArticleKeywordsResponse(keywords.size(), keywords);
    }
    private record InnerKeywordResponse (
        @Schema(description = "키워드 id", example = "1")
        Integer id,

        @Schema(description = "키워드 명")
        String keyword
    ) {

    }
}
