package in.koreatech.koin.domain.language.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;

import in.koreatech.koin.domain.language.dto.LanguagesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Language: 언어", description = "지원 언어 목록")
public interface LanguageApi {

    @Operation(summary = "고정 표시 문구 조회", description = "학식 코너·상점 업종·복덕방 항목·요일·상태·셔틀 분류의 사전 번역입니다. 키로 조회해 표시하며 원본 ID·코드는 유지합니다.")
    @ApiResponse(responseCode = "200")
    @SecurityRequirements
    @GetMapping("/languages/{language}/messages")
    ResponseEntity<Map<String, String>> getMessages(@PathVariable String language);

    @Operation(
        summary = "지원 언어 목록 조회",
        description = "로그인 없이 지원 언어 목록과 기본 언어(`ko-KR`)를 조회합니다."
    )
    @ApiResponse(responseCode = "200")
    @SecurityRequirements
    @GetMapping("/languages")
    ResponseEntity<LanguagesResponse> getLanguages();
}
