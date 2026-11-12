package in.koreatech.koin.domain.community.article.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import in.koreatech.koin.domain.community.article.model.Comment;
import in.koreatech.koin.domain.translation.service.TranslationSourceFields;
import in.koreatech.koin.domain.translation.service.TranslationSourceVersion;
import io.swagger.v3.oas.annotations.media.Schema;

public record ArticleCommentResponse(Integer id, String content, String nickname) {
    public static ArticleCommentResponse from(Comment comment) {
        return new ArticleCommentResponse(comment.getId(), comment.getContent(), comment.getNickname());
    }

    @JsonProperty("translation_source_version")
    @Schema(description = "댓글 번역 요청의 expected_source_version으로 전달할 버전")
    public String getTranslationSourceVersion() {
        return TranslationSourceVersion.of(TranslationSourceFields.message(content));
    }
}
