package in.koreatech.koin.domain.community.articles.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.community.articles.service.ArticleService;
import in.koreatech.koin.domain.community.articles.dto.ArticleResponse;
import in.koreatech.koin.domain.community.articles.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.articles.dto.HotArticleItemResponse;
import in.koreatech.koin.global.auth.UserId;
import in.koreatech.koin.global.ipaddress.IpAddress;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ArticleController implements ArticleApi {

    private final ArticleService articleService;

    @GetMapping("/articles/{id}")
    public ResponseEntity<ArticleResponse> getArticle(
        @UserId Integer userId,
        @PathVariable("id") Integer articleId,
        @IpAddress String ipAddress
    ) {
        ArticleResponse foundArticle = articleService.getArticle(userId, articleId, ipAddress);
        return ResponseEntity.ok().body(foundArticle);
    }

    @GetMapping("/articles")
    public ResponseEntity<ArticlesResponse> getArticles(
        @RequestParam Integer boardId,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit
    ) {
        ArticlesResponse foundArticles = articleService.getArticles(boardId, page, limit);
        return ResponseEntity.ok().body(foundArticles);
    }

    @GetMapping("/articles/hot")
    public ResponseEntity<List<HotArticleItemResponse>> getHotArticles() {
        List<HotArticleItemResponse> hotArticles = articleService.getHotArticles();
        return ResponseEntity.ok().body(hotArticles);
    }

    @GetMapping("/articles/search")
    public ResponseEntity<ArticlesResponse> searchArticles(
        @RequestParam String query,
        @RequestParam(required = false) Integer boardId,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit
    ) {
        ArticlesResponse foundArticles = articleService.searchArticles(query, boardId, page, limit);
        return ResponseEntity.ok().body(foundArticles);
    }
}
