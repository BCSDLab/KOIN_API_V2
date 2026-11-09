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
        description = "로그인 없이 지원 언어 목록과 기본 언어(`ko-KR`)를 조회합니다."
    )
    @ApiResponse(responseCode = "200")
    @SecurityRequirements
    @GetMapping("/languages")
    ResponseEntity<LanguagesResponse> getLanguages();
}
