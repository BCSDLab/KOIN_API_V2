package in.koreatech.koin.domain.community.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.community.dto.ArticleKeywordCreateRequest;
import in.koreatech.koin.domain.community.dto.ArticleKeywordsResponse;
import in.koreatech.koin.domain.community.dto.ArticleResponse;
import in.koreatech.koin.domain.community.dto.ArticleKeywordResponse;
import in.koreatech.koin.domain.community.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.dto.HotArticleItemResponse;
import in.koreatech.koin.domain.community.service.CommunityService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import in.koreatech.koin.global.ipaddress.IpAddress;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CommunityController implements CommunityApi {

    private final CommunityService communityService;

    @GetMapping("/articles/{id}")
    public ResponseEntity<ArticleResponse> getArticle(
        @UserId Integer userId,
        @PathVariable("id") Integer articleId,
        @IpAddress String ipAddress
    ) {
        ArticleResponse foundArticle = communityService.getArticle(userId, articleId, ipAddress);
        return ResponseEntity.ok().body(foundArticle);
    }

    @GetMapping("/articles")
    public ResponseEntity<ArticlesResponse> getArticles(
        @RequestParam Integer boardId,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit
    ) {
        ArticlesResponse foundArticles = communityService.getArticles(boardId, page, limit);
        return ResponseEntity.ok().body(foundArticles);
    }

    @GetMapping("/articles/hot")
    public ResponseEntity<List<HotArticleItemResponse>> getHotArticles() {
        List<HotArticleItemResponse> hotArticles = communityService.getHotArticles();
        return ResponseEntity.ok().body(hotArticles);
    }

    @PostMapping("/articles/keyword")
    public ResponseEntity<ArticleKeywordResponse> createKeyword(
        @Valid @RequestBody ArticleKeywordCreateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        ArticleKeywordResponse response = communityService.createKeyword(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/articles/keyword/{id}")
    public ResponseEntity<Void> deleteKeyword(
        @PathVariable(value = "id") Integer keywordUserMapId,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        communityService.deleteKeyword(userId, keywordUserMapId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/articles/keyword/me")
    public ResponseEntity<ArticleKeywordsResponse> getMyKeywords(
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        ArticleKeywordsResponse response = communityService.getMyKeywords(userId);
        return ResponseEntity.ok(response);
    }

}
