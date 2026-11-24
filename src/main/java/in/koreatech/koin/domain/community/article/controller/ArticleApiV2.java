package in.koreatech.koin.domain.community.article.controller;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.community.article.dto.ArticleResponseV2;
import in.koreatech.koin.global.ipaddress.IpAddress;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Articles V2: 게시글", description = "게시글 정보를 관리한다")
@RequestMapping("/v2/articles")
public interface ArticleApiV2 {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "게시글 단건 조회 V2",
        description = "공지 원문과 한국어 `ai_summary`를 조회합니다. 설정 언어로 자동 표시하려면 조회 후 `ai_summary.translation_source_version`과 설정 언어로 POST /v2/articles/{id}/ai-summary/translations를 호출합니다. 원문은 유지하고 성공한 번역 결과만 표시합니다.")
    @SecurityRequirements
    @GetMapping("/{id}")
    ResponseEntity<ArticleResponseV2> getArticleV2(
        @RequestParam(required = false) Integer boardId,
        @Parameter(in = PATH) @PathVariable("id") Integer articleId,
        @IpAddress String ipAddress
    );
}
