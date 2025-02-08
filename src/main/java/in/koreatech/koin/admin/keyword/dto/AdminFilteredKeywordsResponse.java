package in.koreatech.koin.admin.keyword.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminFilteredKeywordsResponse(
    @Schema(description = "필터링된 키워드 목록", requiredMode = REQUIRED)
    List<InnerFilteredKeywordResponse> keywords
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerFilteredKeywordResponse(
        @Schema(description = "키워드 내용", example = "[ㅇ, ㄴ라]", requiredMode = REQUIRED)
        String keyword
    ) {

        public static InnerFilteredKeywordResponse from(ArticleKeyword articleKeyword) {
            return new InnerFilteredKeywordResponse(
                articleKeyword.getKeyword()
            );
        }
    }

    public static AdminFilteredKeywordsResponse from(List<ArticleKeyword> filteredKeywords) {
        return new AdminFilteredKeywordsResponse(
            filteredKeywords.stream()
                .map(InnerFilteredKeywordResponse::from)
                .toList()
        );
    }
}
