package in.koreatech.koin.domain.community.keyword.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record ArticleKeywordsSuggestionResponse(
    @Schema(description = "추천 키워드 목록",example = """
    ["장학금", "생활관", "수강", "룸메", "컴공"]
    """)
    List<String> keywords
) {

    public static ArticleKeywordsSuggestionResponse from(List<String> keywords) {
        return new ArticleKeywordsSuggestionResponse(keywords);
    }
}
