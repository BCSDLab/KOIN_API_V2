package in.koreatech.koin.domain.community.keyword.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.community.keyword.dto.ArticleKeywordCreateRequest;
import in.koreatech.koin.domain.community.keyword.dto.ArticleKeywordResponse;
import in.koreatech.koin.domain.community.keyword.dto.ArticleKeywordsResponse;
import in.koreatech.koin.domain.community.keyword.dto.ArticleKeywordsSuggestionResponse;
import in.koreatech.koin.domain.community.keyword.dto.KeywordNotificationRequest;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Keyword: 키워드", description = "키워드 알림 정보를 관리한다")
public interface KeywordApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "알림 키워드 생성")
    @PostMapping("/articles/keyword")
    ResponseEntity<ArticleKeywordResponse> createKeyword(
        @Valid @RequestBody ArticleKeywordCreateRequest request,
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "알림 키워드 삭제")
    @DeleteMapping("/articles/keyword/{id}")
    ResponseEntity<Void> deleteKeyword(
        @PathVariable(value = "id") Integer keywordUserMapId,
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "자신의 알림 키워드 전체 조회")
    @GetMapping("/articles/keyword/me")
    ResponseEntity<ArticleKeywordsResponse> getMyKeywords(
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "알림 키워드 추천")
    @GetMapping("/articles/keyword/suggestions")
    ResponseEntity<ArticleKeywordsSuggestionResponse> suggestKeywords(
    );

    @Operation(summary = "키워드 알림 전송", hidden = true)
    @PostMapping("/notification")
    ResponseEntity<Void> pushKeywordNotification(
        @Valid @RequestBody KeywordNotificationRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
