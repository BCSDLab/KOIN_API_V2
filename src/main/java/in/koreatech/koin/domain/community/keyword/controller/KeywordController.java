package in.koreatech.koin.domain.community.keyword.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.community.keyword.dto.FilteredKeywordsResponse;
import in.koreatech.koin.domain.community.keyword.dto.KeywordFilterRequest;
import in.koreatech.koin.domain.community.keyword.dto.ArticleKeywordCreateRequest;
import in.koreatech.koin.domain.community.keyword.dto.ArticleKeywordResponse;
import in.koreatech.koin.domain.community.keyword.dto.ArticleKeywordsResponse;
import in.koreatech.koin.domain.community.keyword.dto.ArticleKeywordsSuggestionResponse;
import in.koreatech.koin.domain.community.keyword.dto.KeywordNotificationRequest;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keyword.service.KeywordService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/articles/keyword")
public class KeywordController implements KeywordApi{

    private final KeywordService keywordService;

    @PostMapping()
    public ResponseEntity<ArticleKeywordResponse> createKeyword(
        @Valid @RequestBody ArticleKeywordCreateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        ArticleKeywordResponse response = keywordService.createKeyword(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKeyword(
        @PathVariable(value = "id") Integer keywordUserMapId,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        keywordService.deleteKeyword(userId, keywordUserMapId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<ArticleKeywordsResponse> getMyKeywords(
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        ArticleKeywordsResponse response = keywordService.getMyKeywords(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ArticleKeywordsSuggestionResponse> suggestKeywords(
    ) {
        ArticleKeywordsSuggestionResponse response = keywordService.suggestKeywords();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/notification")
    public ResponseEntity<Void> pushKeywordNotification(
        @Valid @RequestBody KeywordNotificationRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        keywordService.sendKeywordNotification(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/filter")
    public ResponseEntity<Void> toggleKeywordFilter(
        @Valid @RequestBody KeywordFilterRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        keywordService.filterKeyword(request.keyword(), request.isFiltered());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/filtered")
    public ResponseEntity<FilteredKeywordsResponse> getFilteredKeywords(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        FilteredKeywordsResponse response = keywordService.getFilteredKeywords();
        return ResponseEntity.ok(response);
    }

}
