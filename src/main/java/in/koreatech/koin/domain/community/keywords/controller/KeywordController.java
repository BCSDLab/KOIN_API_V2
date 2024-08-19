package in.koreatech.koin.domain.community.keywords.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.community.keywords.dto.ArticleKeywordCreateRequest;
import in.koreatech.koin.domain.community.keywords.dto.ArticleKeywordResponse;
import in.koreatech.koin.domain.community.keywords.service.KeywordService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class KeywordController implements KeywordApi{

    private final KeywordService keywordService;

    @PostMapping("/articles/keyword")
    public ResponseEntity<ArticleKeywordResponse> createKeyword(
        @Valid @RequestBody ArticleKeywordCreateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        ArticleKeywordResponse response = keywordService.createKeyword(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/articles/keyword/{id}")
    public ResponseEntity<Void> deleteKeyword(
        @PathVariable(value = "id") Integer keywordUserMapId,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        keywordService.deleteKeyword(userId, keywordUserMapId);
        return ResponseEntity.noContent().build();
    }
}
