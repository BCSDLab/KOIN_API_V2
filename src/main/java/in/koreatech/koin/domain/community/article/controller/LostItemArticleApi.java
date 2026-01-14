package in.koreatech.koin.domain.community.article.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;
import static in.koreatech.koin.global.code.ApiResponseCode.*;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.community.article.dto.FoundLostItemArticleCountResponse;
import in.koreatech.koin.domain.community.article.dto.LostItemArticleResponse;
import in.koreatech.koin.domain.community.article.dto.LostItemArticleStatisticsResponse;
import in.koreatech.koin.domain.community.article.dto.LostItemArticlesRequest;
import in.koreatech.koin.domain.community.article.dto.LostItemArticlesResponse;
import in.koreatech.koin.domain.community.article.dto.NotFoundLostItemArticleCountResponse;
import in.koreatech.koin.domain.community.article.model.filter.LostItemAuthorFilter;
import in.koreatech.koin.domain.community.article.model.filter.LostItemCategoryFilter;
import in.koreatech.koin.domain.community.article.model.filter.LostItemFoundStatus;
import in.koreatech.koin.domain.community.article.model.filter.LostItemSortType;
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

@Tag(name = "(Normal) LostItem Articles: 분실물 게시글", description = "분실물 게시글 정보를 관리한다")
@RequestMapping("/articles")
public interface LostItemArticleApi {

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

    @ApiResponseCodes({
        OK,
        UNAUTHORIZED_USER,
    })
    @Operation(summary = "분실물 게시글 목록 조회 V2", description = """
        ### 분실물 게시글 목록 조회 V2 변경점
        - Request Param 추가: foundStatus, category, sort, authorType
        - 내 게시물 필터를 설정할 경우, 토큰을 포함하여 요청하지 않으면 401 응답이 반환됩니다
        """)
    @GetMapping("/lost-item/v2")
    ResponseEntity<LostItemArticlesResponse> getLostItemArticlesV2(
        @Parameter(description = "분실물 타입 (LOST: 분실물, FOUND: 습득물)")
        @RequestParam(required = false) String type,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false, name = "category", defaultValue = "ALL") LostItemCategoryFilter itemCategory,
        @Parameter(description = "물품 상태 (ALL: 전체, FOUND: 찾음, NOT_FOUND: 찾는 중)")
        @RequestParam(required = false, defaultValue = "ALL") LostItemFoundStatus foundStatus,
        @Parameter(description = "정렬 순서 (LATEST: 최신순(default), OLDEST: 오래된순)")
        @RequestParam(required = false, defaultValue = "LATEST") LostItemSortType sort,
        @Parameter(description = "내 게시물 (ALL: 전체, MY: 내 게시물)")
        @RequestParam(required = false, name = "author", defaultValue = "ALL") LostItemAuthorFilter authorType,
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
    @Operation(summary = "분실물 게시글 통계 조회", description = """
        ### 분실물 게시글 통계 조회
        - found_count : 주인 찾음 상태의 게시글 개수
        - not_found_count : 주인 찾는 중 상태의 게시글 개수
        """)
    @GetMapping("/lost-item/stats")
    ResponseEntity<LostItemArticleStatisticsResponse> getLostItemArticlesStats();
}
