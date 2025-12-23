package in.koreatech.koin.domain.community.article.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;
import static in.koreatech.koin.global.code.ApiResponseCode.*;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.community.article.dto.ArticleHotKeywordResponse;
import in.koreatech.koin.domain.community.article.dto.ArticleResponse;
import in.koreatech.koin.domain.community.article.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.article.dto.FoundLostItemArticleCountResponse;
import in.koreatech.koin.domain.community.article.dto.HotArticleItemResponse;
import in.koreatech.koin.domain.community.article.dto.LostItemArticleResponse;
import in.koreatech.koin.domain.community.article.dto.LostItemArticlesRequest;
import in.koreatech.koin.domain.community.article.dto.LostItemArticlesResponse;
import in.koreatech.koin.domain.community.article.model.LostItemFoundStatus;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import in.koreatech.koin.global.code.ApiResponseCodes;
import in.koreatech.koin.global.ipaddress.IpAddress;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Articles: 게시글", description = "게시글 정보를 관리한다")
@RequestMapping("/articles")
public interface ArticleApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "게시글 단건 조회")
    @GetMapping("/{id}")
    ResponseEntity<ArticleResponse> getArticle(
        @RequestParam(required = false) Integer boardId,
        @Parameter(in = PATH) @PathVariable("id") Integer articleId,
        @IpAddress String ipAddress
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "게시글 목록 조회")
    @GetMapping("")
    ResponseEntity<ArticlesResponse> getArticles(
        @RequestParam(required = false) Integer boardId,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @UserId Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "인기 게시글 목록 조회")
    @GetMapping("/hot")
    ResponseEntity<List<HotArticleItemResponse>> getHotArticles();

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "게시글 검색")
    @GetMapping("/search")
    ResponseEntity<ArticlesResponse> searchArticles(
        @RequestParam String query,
        @RequestParam(required = false) Integer boardId,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @IpAddress String ipAddress,
        @UserId Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
        }
    )
    @Operation(summary = "분실물 게시글 검색")
    @GetMapping("/lost-item/search")
    ResponseEntity<LostItemArticlesResponse> searchArticles(
        @RequestParam String query,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @IpAddress String ipAddress,
        @UserId Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200")
        }
    )
    @Operation(summary = "많이 검색되는 키워드(검색 화면)")
    @GetMapping("/hot/keyword")
    ResponseEntity<ArticleHotKeywordResponse> getArticlesHotKeyword(
        @RequestParam Integer count
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "분실물 게시글 목록 조회")
    @GetMapping("/lost-item")
    ResponseEntity<LostItemArticlesResponse> getLostItemArticles(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @UserId Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "분실물 게시글 목록 조회 V2", description = """
        ### 분실물 게시글 목록 조회 V2 변경점
        - Request Param 추가: foundStatus (ALL, FOUND, NOT_FOUND)
          - ALL : 모든 분실물 게시글 조회 (Default)
          - FOUND : '주인 찾음' 상태인 게시글 조회
          - NOT_FOUND : '찾는 중' 상태인 게시글 조회
        """)
    @GetMapping("/lost-item/v2")
    ResponseEntity<LostItemArticlesResponse> getLostItemArticlesV2(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false, defaultValue = "ALL") LostItemFoundStatus foundStatus,
        @UserId Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "분실물 게시글 단건 조회")
    @GetMapping("/lost-item/{id}")
    ResponseEntity<LostItemArticleResponse> getLostItemArticle(
        @Parameter(in = PATH) @PathVariable("id") Integer articleId,
        @UserId Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "분실물 게시글 등록")
    @PostMapping("/lost-item")
    ResponseEntity<LostItemArticleResponse> createLostItemArticle(
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId,
        @RequestBody @Valid LostItemArticlesRequest lostItemArticlesRequest
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "분실물 게시글 삭제")
    @DeleteMapping("/lost-item/{id}")
    ResponseEntity<Void> deleteLostItemArticle(
        @PathVariable("id") Integer articleId,
        @Auth(permit = {STUDENT, COUNCIL}) Integer councilId
    );

    @ApiResponseCodes({
        NO_CONTENT,
        FORBIDDEN_AUTHOR,
        DUPLICATE_FOUND_STATUS
    })
    @Operation(summary = "분실물 게시글 찾음 처리")
    @PostMapping("/lost-item/{id}/found")
    ResponseEntity<Void> markLostItemArticleAsFound(
        @PathVariable("id") Integer articleId,
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId
    );

    @ApiResponseCodes({
        OK
    })
    @Operation(summary = "주인 찾음 상태인 분실물 게시글 총 개수 조회")
    @GetMapping("/lost-item/found/count")
    ResponseEntity<FoundLostItemArticleCountResponse> getFoundLostItemArticlesCount();
}
