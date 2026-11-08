package in.koreatech.koin.domain.language.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import in.koreatech.koin.domain.language.dto.LanguagesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Language: 언어", description = "지원 언어 목록")
public interface LanguageApi {

    @Operation(
        summary = "지원 언어 목록 조회",
        description = "로그인 없이 서버 기본 언어와 표시 순서에 따른 지원 언어 목록을 조회합니다. "
            + "UI 언어는 로그인 여부와 관계없이 기기·브라우저에 저장한 선택을 유지하며, 이 응답은 현재 선택값을 포함하지 않습니다."
    )
    @ApiResponse(responseCode = "200")
    @SecurityRequirements
    @GetMapping("/languages")
    ResponseEntity<LanguagesResponse> getLanguages();
}
