package in.koreatech.koin.domain.community.keywords.dto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import in.koreatech.koin.domain.community.keywords.model.ArticleKeywordSuggestCache;
import io.swagger.v3.oas.annotations.media.Schema;

public record ArticleKeywordsSuggestionResponse(
    @Schema(example = """
    ["장학금", "생활관", "수강", "룸메", "컴공"]
    """)
    List<String> keywords
) {

    public static ArticleKeywordsSuggestionResponse from(
        List<ArticleKeywordSuggestCache> hotKeywords,
        List<String> userKeywords) {

        List<String> validUserKeywords = Optional.ofNullable(userKeywords).orElse(Collections.emptyList());

        List<String> suggestions = hotKeywords.stream()
            .filter(hotKeyword -> !validUserKeywords.contains(hotKeyword.getKeyword()))
            .map(ArticleKeywordSuggestCache::getKeyword)
            .limit(5)
            .collect(Collectors.toList());

        return new ArticleKeywordsSuggestionResponse(suggestions);
    }
}
