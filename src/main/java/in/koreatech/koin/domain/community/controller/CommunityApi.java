package in.koreatech.koin.domain.community.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.community.dto.ArticleResponse;
import in.koreatech.koin.domain.community.dto.ArticlesResponse;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static in.koreatech.koin.domain.user.model.UserType.USER;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.ipaddress.IpAddress;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Community: 커뮤니티", description = "커뮤니티 정보를 관리한다")
public interface CommunityApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "게시글 단건 조회")
    @GetMapping("/articles/{id}")
    ResponseEntity<ArticleResponse> getArticle(
        @Auth(permit = {USER, STUDENT}, anonymous = true) Long userId,
        @Parameter(in = PATH) @PathVariable("id") Long articleId,
        @IpAddress String ipAddress
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "게시글 목록 조회")
    @GetMapping("/articles")
    ResponseEntity<ArticlesResponse> getArticles(
        @RequestParam Long boardId,
        @RequestParam(required = false) Long page,
        @RequestParam(required = false) Long limit
    );
}
