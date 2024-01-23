package in.koreatech.koin.domain.community.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.community.dto.ArticleResponse;
import in.koreatech.koin.domain.community.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.service.CommunityService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CommunityController {

    private static final String AUTHORIZATION = "Authorization";

    private final CommunityService communityService;

    @GetMapping("/articles/{id}")
    public ResponseEntity<ArticleResponse> getArticle(@PathVariable Long id,
        @RequestHeader(name = AUTHORIZATION, required = false) String loginToken) {
        ArticleResponse foundArticle = communityService.getArticle(id, loginToken);
        return ResponseEntity.ok().body(foundArticle);
    }

    @GetMapping("/articles")
    public ResponseEntity<ArticlesResponse> getArticles(
        @RequestParam Long boardId,
        @RequestParam(required = false) Long page,
        @RequestParam(required = false) Long limit
    ) {
        ArticlesResponse foundArticles = communityService.getArticles(boardId, page, limit);
        return ResponseEntity.ok().body(foundArticles);
    }
}
