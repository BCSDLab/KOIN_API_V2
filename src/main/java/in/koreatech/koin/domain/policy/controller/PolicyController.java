package in.koreatech.koin.domain.policy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.policy.dto.PolicyResponse;
import in.koreatech.koin.domain.policy.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "(Normal) Policy: 약관·개인정보처리방침")
public class PolicyController {
    private final PolicyService service;

    @Operation(summary = "약관·개인정보처리방침 조회", description = "검수된 외국어 문서가 없으면 한국어 원문을 반환합니다. language와 is_fallback을 확인하세요.")
    @ApiResponse(responseCode = "200", description = "문서 조회 성공")
    @ApiResponse(responseCode = "400", description = "지원하지 않는 문서 종류 또는 언어")
    @SecurityRequirements
    @GetMapping("/policies/{type}")
    public PolicyResponse get(
        @Parameter(description = "terms: 이용약관, privacy: 개인정보처리방침") @PathVariable String type,
        @Parameter(description = "GET /languages의 코드") @RequestParam(defaultValue = "ko-KR") String language
    ) {
        return service.get(type, language);
    }
}
