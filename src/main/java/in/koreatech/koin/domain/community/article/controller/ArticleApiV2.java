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
    @Operation(summary = "게시글 단건 조회 V2", description = """
        게시글 원문과 AI 요약을 분리해 반환합니다. `ai_summary.status`는 SUCCESS, PENDING, UNAVAILABLE입니다.
        SUCCESS이면 최대 3개의 아이콘·텍스트 항목을 표시하고, PENDING이면 요약 준비 중, UNAVAILABLE이면 원문을 표시합니다.

        새 한국어 요약은 항목당 40~70자를 목표로 최대 100자(공백 포함)로 생성합니다.
        기존에 저장한 긴 요약은 새 요약이 성공할 때까지 반환될 수 있으므로 클라이언트에서 100자 기준으로 잘라내지 않습니다.
        이 API는 표시 언어에 맞춰 번역하지 않습니다. 요약 번역 API와 원문 버전 필드는 후속 구현 대상입니다.
        """)
    @GetMapping("/{id}")
    ResponseEntity<ArticleResponseV2> getArticleV2(
        @RequestParam(required = false) Integer boardId,
        @Parameter(in = PATH) @PathVariable("id") Integer articleId,
        @IpAddress String ipAddress
    );
}
