package in.koreatech.koin.admin.lostItem.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.lostItem.dto.AdminLostItemArticlesResponse;
import in.koreatech.koin.admin.lostItem.dto.AdminModifyLostItemArticleReportStatusRequest;
import in.koreatech.koin._common.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) LostItemArticle: 분실물 게시글", description = "관리자 권한으로 분실물 게시글을 관리한다")
public interface AdminLostItemArticleApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "분실물 게시글 조회")
    @GetMapping("/admin/articles/lost-item")
    ResponseEntity<AdminLostItemArticlesResponse> getLostItemArticles(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 분실물 게시글 신고 상태 변경")
    @PutMapping("/admin/articles/lost-item/reports/{id}")
    ResponseEntity<Void> modifyLostItemReportStatus(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody @Valid AdminModifyLostItemArticleReportStatusRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 분실물 게시글 삭제")
    @DeleteMapping("/admin/articles/lost-item/{id}")
    ResponseEntity<Void> deleteLostItemArticle(
        @Parameter(in = PATH) @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
