package in.koreatech.koin.domain.community.article.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.community.article.dto.ArticleCommentResponse;
import in.koreatech.koin.domain.community.article.service.ArticleCommentQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "(Normal) Article: 게시글")
public class ArticleCommentController {
    private final ArticleCommentQueryService service;

    @Operation(summary = "게시글 댓글 조회", description = "삭제되지 않은 댓글과 번역 원문 버전을 반환합니다.")
    @SecurityRequirements
    @GetMapping("/articles/{articleId}/comments")
    public List<ArticleCommentResponse> getComments(@PathVariable Integer articleId) {
        return service.getComments(articleId);
    }
}
