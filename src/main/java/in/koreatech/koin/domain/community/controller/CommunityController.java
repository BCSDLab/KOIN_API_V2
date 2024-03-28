package in.koreatech.koin.domain.community.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.community.dto.ArticleResponse;
import in.koreatech.koin.domain.community.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.dto.HotArticleItemResponse;
import in.koreatech.koin.domain.community.service.CommunityService;
import in.koreatech.koin.global.auth.UserId;
import in.koreatech.koin.global.ipaddress.IpAddress;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CommunityController implements CommunityApi {

    private final CommunityService communityService;

    @GetMapping("/articles/{id}")
    public ResponseEntity<ArticleResponse> getArticle(
        @UserId Long userId,
        @PathVariable("id") Long articleId,
        @IpAddress String ipAddress
    ) {
        ArticleResponse foundArticle = communityService.getArticle(userId, articleId, ipAddress);
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

    @GetMapping("/articles/hot/list")
    public ResponseEntity<List<HotArticleItemResponse>> getHotArticles() {
        List<HotArticleItemResponse> hotArticles = communityService.getHotArticles();
        return ResponseEntity.ok().body(hotArticles);
    }
}
