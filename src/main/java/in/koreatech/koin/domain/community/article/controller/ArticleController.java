package in.koreatech.koin.domain.community.article.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.community.article.dto.ArticleHotKeywordResponse;
import in.koreatech.koin.domain.community.article.dto.ArticleResponse;
import in.koreatech.koin.domain.community.article.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.article.dto.HotArticleItemResponse;
import in.koreatech.koin.domain.community.article.dto.LostItemArticleResponse;
import in.koreatech.koin.domain.community.article.dto.LostItemArticlesRequest;
import in.koreatech.koin.domain.community.article.dto.LostItemArticlesResponse;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin.domain.community.article.service.ArticleService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.ipaddress.IpAddress;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/articles")
public class ArticleController implements ArticleApi {

    private final ArticleService articleService;

    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getArticle(
        @RequestParam(required = false) Integer boardId,
        @PathVariable("id") Integer articleId,
        @IpAddress String ipAddress
    ) {
        ArticleResponse foundArticle = articleService.getArticle(boardId, articleId, ipAddress);
        return ResponseEntity.ok().body(foundArticle);
    }

    @GetMapping()
    public ResponseEntity<ArticlesResponse> getArticles(
        @RequestParam Integer boardId,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit
    ) {
        ArticlesResponse foundArticles = articleService.getArticles(boardId, page, limit);
        return ResponseEntity.ok().body(foundArticles);
    }

    @GetMapping("/hot")
    public ResponseEntity<List<HotArticleItemResponse>> getHotArticles() {
        List<HotArticleItemResponse> hotArticles = articleService.getHotArticles();
        return ResponseEntity.ok().body(hotArticles);
    }

    @GetMapping("/search")
    public ResponseEntity<ArticlesResponse> searchArticles(
        @RequestParam String query,
        @RequestParam(required = false) Integer boardId,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @IpAddress String ipAddress
    ) {
        ArticlesResponse foundArticles = articleService.searchArticles(query, boardId, page, limit, ipAddress);
        return ResponseEntity.ok().body(foundArticles);
    }

    @GetMapping("/hot/keyword")
    public ResponseEntity<ArticleHotKeywordResponse> getArticlesHotKeyword(
        @RequestParam Integer count
    ) {
        ArticleHotKeywordResponse response = articleService.getArticlesHotKeyword(count);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/lostitem")
    public ResponseEntity<LostItemArticlesResponse> getLostItemArticles(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit
    ) {
        LostItemArticlesResponse response = articleService.getLostItemArticles(page, limit);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/lostitem/{id}")
    public ResponseEntity<LostItemArticleResponse> getLostItemArticle(
        @PathVariable("id") Integer articleId
    ) {
        return ResponseEntity.ok().body(articleService.getLostItemArticle(articleId));
    }

    @PostMapping("/lostitem")
    public ResponseEntity<Void> createLostItemArticle(
        @Auth(permit = {COUNCIL}) Integer councilId,
        @RequestBody @Valid LostItemArticlesRequest lostItemArticlesRequest
    ) {
        articleService.createLostItemArticle(councilId, lostItemArticlesRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/lostitem/{id}")
    public ResponseEntity<Void> deleteLostItemArticle(
        @PathVariable("id") Integer articleId,
        @Auth(permit = {COUNCIL}) Integer councilId
    ) {
        articleService.deleteLostItemArticle(articleId);
        return ResponseEntity.noContent().build();
    }
}
