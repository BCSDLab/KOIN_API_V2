package in.koreatech.koin.domain.community.articles.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

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

import in.koreatech.koin.domain.community.articles.dto.ArticleHotKeywordResponse;
import in.koreatech.koin.domain.community.keywords.dto.ArticleKeywordCreateRequest;
import in.koreatech.koin.domain.community.keywords.dto.ArticleKeywordResponse;
import in.koreatech.koin.domain.community.articles.dto.ArticleResponse;
import in.koreatech.koin.domain.community.articles.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.articles.dto.HotArticleItemResponse;
import in.koreatech.koin.domain.community.articles.service.CommunityService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import in.koreatech.koin.global.ipaddress.IpAddress;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/articles")
public class CommunityController implements CommunityApi {

    private final CommunityService communityService;

    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getArticle(
        @UserId Integer userId,
        @PathVariable("id") Integer articleId,
        @IpAddress String ipAddress
    ) {
        ArticleResponse foundArticle = communityService.getArticle(userId, articleId, ipAddress);
        return ResponseEntity.ok().body(foundArticle);
    }

    @GetMapping()
    public ResponseEntity<ArticlesResponse> getArticles(
        @RequestParam Integer boardId,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit
    ) {
        ArticlesResponse foundArticles = communityService.getArticles(boardId, page, limit);
        return ResponseEntity.ok().body(foundArticles);
    }

    @GetMapping("/hot")
    public ResponseEntity<List<HotArticleItemResponse>> getHotArticles() {
        List<HotArticleItemResponse> hotArticles = communityService.getHotArticles();
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
        ArticlesResponse foundArticles = communityService.searchArticles(query, boardId, page, limit, ipAddress);
        return ResponseEntity.ok().body(foundArticles);
    }

    @GetMapping("/articles/hot/keyword")
    public ResponseEntity<ArticleHotKeywordResponse> getArticlesHotKeyword(
        @RequestParam Integer count
    ) {
        ArticleHotKeywordResponse response = communityService.getArticlesHotKeyword(count);
        return ResponseEntity.ok().body(response);
    }
}
