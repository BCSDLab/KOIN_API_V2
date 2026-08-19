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
    @Operation(summary = "게시글 단건 조회 V2", description = "게시글 원문과 AI 요약을 분리해 반환한다.")
    @GetMapping("/{id}")
    ResponseEntity<ArticleResponseV2> getArticleV2(
        @RequestParam(required = false) Integer boardId,
        @Parameter(in = PATH) @PathVariable("id") Integer articleId,
        @IpAddress String ipAddress
    );
}
