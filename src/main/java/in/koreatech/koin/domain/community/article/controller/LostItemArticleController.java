package in.koreatech.koin.domain.community.article.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.community.article.dto.FoundLostItemArticleCountResponse;
import in.koreatech.koin.domain.community.article.dto.LostItemArticleResponse;
import in.koreatech.koin.domain.community.article.dto.LostItemArticlesRequest;
import in.koreatech.koin.domain.community.article.dto.LostItemArticlesResponse;
import in.koreatech.koin.domain.community.article.model.LostItemFoundStatus;
import in.koreatech.koin.domain.community.article.service.LostItemArticleService;
import in.koreatech.koin.domain.community.article.service.LostItemFoundService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import in.koreatech.koin.global.ipaddress.IpAddress;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/articles")
public class LostItemArticleController implements LostItemArticleApi {

    private final LostItemArticleService lostItemArticleService;
    private final LostItemFoundService lostItemFoundService;

    @GetMapping("/lost-item/search")
    public ResponseEntity<LostItemArticlesResponse> searchArticles(
        @RequestParam String query,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @IpAddress String ipAddress,
        @UserId Integer userId
    ) {
        LostItemArticlesResponse foundArticles = lostItemArticleService.searchLostItemArticles(query, page, limit, ipAddress,
            userId);
        return ResponseEntity.ok().body(foundArticles);
    }

    @GetMapping("/lost-item")
    public ResponseEntity<LostItemArticlesResponse> getLostItemArticles(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @UserId Integer userId
    ) {
        LostItemArticlesResponse response = lostItemArticleService.getLostItemArticles(type, page, limit, userId);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/lost-item/v2")
    public ResponseEntity<LostItemArticlesResponse> getLostItemArticlesV2(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false, defaultValue = "ALL") LostItemFoundStatus foundStatus,
        @UserId Integer userId
    ) {
        LostItemArticlesResponse response = lostItemArticleService.getLostItemArticlesV2(type, page, limit, userId, foundStatus);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/lost-item/{id}")
    public ResponseEntity<LostItemArticleResponse> getLostItemArticle(
        @PathVariable("id") Integer articleId,
        @UserId Integer userId
    ) {
        return ResponseEntity.ok().body(lostItemArticleService.getLostItemArticle(articleId, userId));
    }

    @PostMapping("/lost-item")
    public ResponseEntity<LostItemArticleResponse> createLostItemArticle(
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer studentId,
        @RequestBody @Valid LostItemArticlesRequest lostItemArticlesRequest
    ) {
        LostItemArticleResponse response = lostItemArticleService.createLostItemArticle(studentId, lostItemArticlesRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/lost-item/{id}")
    public ResponseEntity<Void> deleteLostItemArticle(
        @PathVariable("id") Integer articleId,
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId
    ) {
        lostItemArticleService.deleteLostItemArticle(articleId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/lost-item/{id}/found")
    public ResponseEntity<Void> markLostItemArticleAsFound(
        @PathVariable("id") Integer articleId,
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId
    ) {
        lostItemFoundService.markAsFound(userId, articleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lost-item/found/count")
    public ResponseEntity<FoundLostItemArticleCountResponse> getFoundLostItemArticlesCount() {
        FoundLostItemArticleCountResponse response = lostItemFoundService.countFoundArticles();
        return ResponseEntity.ok().body(response);
    }
}
