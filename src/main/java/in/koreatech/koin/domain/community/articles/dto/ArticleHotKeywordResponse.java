
package in.koreatech.koin.domain.community.articles.dto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.media.Schema;

public record ArticleHotKeywordResponse (
    @Schema(example = """
    ["장학금", "생활관", "수강", "룸메", "컴공"]
    """)
    List<String> keywords
) {
    public static ArticleHotKeywordResponse from(List<String> results) {
        if (results == null || results.isEmpty()) {
            return new ArticleHotKeywordResponse(Collections.emptyList());
        }

        return new ArticleHotKeywordResponse(results);
    }
}
