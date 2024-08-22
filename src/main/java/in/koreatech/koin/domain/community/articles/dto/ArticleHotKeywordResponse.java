
package in.koreatech.koin.domain.community.articles.dto;

import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.media.Schema;

public record ArticleHotKeywordResponse (
    @Schema(example = """
    ["장학금", "생활관", "수강", "룸메", "컴공"]
    """)
    List<String> keywords
) {
    public static ArticleHotKeywordResponse from(List<Object[]> results) {
        List<String> keywords = results.stream()
            .map(result -> (String) result[0])  // Object[] 배열에서 키워드만 추출
            .collect(Collectors.toList());

        return new ArticleHotKeywordResponse(keywords);
    }
}
