package in.koreatech.koin.domain.community.keyword.dto;

import java.util.List;
import java.util.stream.Collectors;

import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import io.swagger.v3.oas.annotations.media.Schema;

public record ArticleKeywordsResponse (
    @Schema(description = "키워드 수", example = "3")
    Integer count,

    @Schema(description = "나의 키워드 목록", example = """
    ["장학금", "생활관", "수강", "룸메", "컴공"]
    """)
    List<InnerKeywordResponse> keywords
) {

    public static ArticleKeywordsResponse from(List<ArticleKeywordUserMap> articleKeywordUserMaps) {
        if (articleKeywordUserMaps.isEmpty()) {
            return new ArticleKeywordsResponse(0, List.of());
        }

        List<InnerKeywordResponse> keywords = articleKeywordUserMaps.stream()
            .map(userMap -> new InnerKeywordResponse(
                userMap.getId(),
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
