package in.koreatech.koin.domain.translation.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.translation.dto.SourceLanguageData;
import in.koreatech.koin.domain.translation.dto.SourceLanguageRequest;
import in.koreatech.koin.domain.translation.dto.TranslationResponse;
import in.koreatech.koin.domain.translation.service.SourceLanguageService;
import in.koreatech.koin.domain.translation.service.TranslationFailureException;
import in.koreatech.koin.global.auth.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "(Normal) Translation: 번역")
public class SourceLanguageController {
    private final SourceLanguageService service;

    @Operation(summary = "원문 언어·번역 버튼 표시 여부 확인", description = """
        서버에서 원문을 읽어 실제 언어를 판별합니다. 작성자 설정 언어는 사용하지 않습니다.
        data.translation_required=true일 때 번역 버튼을 표시합니다. 실제 번역은 버튼 클릭 후 기존 번역 POST로 요청합니다.
        source_version이 화면의 translation_source_version과 다르면 원문을 다시 조회합니다.
        """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "판별 완료. 같은 원문은 판별 결과 재사용"),
        @ApiResponse(responseCode = "401", description = "쪽지·비공개 원문의 인증 필요"),
        @ApiResponse(responseCode = "403", description = "원문 접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "원문 없음"),
        @ApiResponse(responseCode = "409", description = "판별 중 원문 변경. 원문 재조회"),
        @ApiResponse(responseCode = "422", description = "언어 판별 대상 없음 또는 입력 길이 초과"),
        @ApiResponse(responseCode = "502", description = "잘못된 AI 응답"),
        @ApiResponse(responseCode = "503", description = "일시 장애·호출 제한·원문 준비 전"),
        @ApiResponse(responseCode = "504", description = "판별 제한 시간 초과")
    })
    @PostMapping("/translations/source-language")
    public ResponseEntity<TranslationResponse<SourceLanguageData>> detect(
        @RequestBody @Valid SourceLanguageRequest request, @UserId Integer userId) {
        TranslationResponse<SourceLanguageData> response;
        long retryAfter = 30;
        try {
            response = service.detect(request, userId);
        } catch (TranslationFailureException exception) {
            response = new TranslationResponse<>(null, "FAILED", request.targetLanguage(), null,
                exception.getReason(), exception.getReason().isRetryable(), null);
            if (exception.getRetryAfter() != null) {
                retryAfter = Math.max(retryAfter, exception.getRetryAfter().getSeconds()
                    + (exception.getRetryAfter().getNano() > 0 ? 1 : 0));
            }
        }
        var builder = ResponseEntity.status(TranslationController.postStatus(response)).cacheControl(CacheControl.noStore());
        if (response.retryable()) {
            builder.header(HttpHeaders.RETRY_AFTER, Long.toString(retryAfter));
        }
        return builder.body(response);
    }
}
