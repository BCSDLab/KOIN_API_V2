package in.koreatech.koin.domain.translation.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import in.koreatech.koin.domain.translation.dto.*;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import in.koreatech.koin.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Translation: 번역", description = "콘텐츠 번역 및 결과 조회")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "SUCCESS: 완료 · UNAVAILABLE: 제공 불가 · FAILED: 실패. 실패 시 Retry-After 이후 POST로 재시도합니다."),
    @ApiResponse(responseCode = "400", description = "필수값·언어 코드·원문 버전 형식 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "401", description = "쪽지 번역에 본인 인증이 필요합니다.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "403", description = "현재 접근 권한 없음 또는 쿠키 Origin/CSRF 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "원문·방·메시지·결과가 없거나 결과 보관 기간이 지났습니다.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "409", description = "SOURCE_CHANGED: 원문이 변경됐습니다. 원문을 다시 조회한 뒤 새 버전으로 요청하세요.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public interface TranslationApi {
    @Operation(summary = "공지 AI 요약 번역", description = "현재 공지의 AI 요약을 번역합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = """
            | 필드 | 설명 |
            | --- | --- |
            | `target_language` | 번역할 언어 코드. `GET /languages`의 `code`를 사용합니다. |
            | `expected_source_version` | 원문 변경 확인용 버전. 공지 조회 응답의 `ai_summary.translation_source_version`을 그대로 보냅니다. |
            """))
    @SecurityRequirements
    @ApiResponse(responseCode = "202", description = "PENDING: 처리 중. Retry-After 이후 GET /translations/{translationId}로 조회합니다.")
    @PostMapping("/v2/articles/{id}/ai-summary/translations")
    ResponseEntity<TranslationResponse<TranslationData.Summary>> summary(
        @PathVariable Integer id, @RequestBody @Valid TranslationRequest request);

    @Operation(summary = "분실물 게시글 번역", description = "분실물의 습득 장소와 내용을 번역합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = """
            | 필드 | 설명 |
            | --- | --- |
            | `target_language` | 번역할 언어 코드. `GET /languages`의 `code`를 사용합니다. |
            | `expected_source_version` | 원문 변경 확인용 버전. 분실물 조회 응답의 `translation_source_version`을 그대로 보냅니다. |
            """))
    @SecurityRequirements
    @ApiResponse(responseCode = "202", description = "PENDING: 처리 중. Retry-After 이후 GET /translations/{translationId}로 조회합니다.")
    @PostMapping("/articles/lost-item/{id}/translations")
    ResponseEntity<TranslationResponse<TranslationData.LostItem>> lostItem(
        @PathVariable Integer id, @RequestBody @Valid TranslationRequest request);

    @Operation(summary = "팀원 모집글 번역", description = "모집글의 제목·소개·지원 자격을 번역합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = """
            | 필드 | 설명 |
            | --- | --- |
            | `target_language` | 번역할 언어 코드. `GET /languages`의 `code`를 사용합니다. |
            | `expected_source_version` | 원문 변경 확인용 버전. 모집글 조회 응답의 `translation_source_version`을 그대로 보냅니다. |
            """))
    @SecurityRequirements
    @ApiResponse(responseCode = "202", description = "PENDING: 처리 중. Retry-After 이후 GET /translations/{translationId}로 조회합니다.")
    @PostMapping("/team-recruitments/{id}/translations")
    ResponseEntity<TranslationResponse<TranslationData.Recruitment>> recruitment(
        @PathVariable Integer id, @RequestBody @Valid TranslationRequest request);

    @Operation(summary = "분실물 쪽지 번역", description = "현재 방 참여자만 텍스트 쪽지를 번역할 수 있습니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = """
            | 필드 | 설명 |
            | --- | --- |
            | `target_language` | 번역할 언어 코드. `GET /languages`의 `code`를 사용합니다. |
            | `expected_source_version` | 원문 변경 확인용 버전. 분실물 쪽지 조회 응답에서 번역할 메시지의 `translation_source_version`을 그대로 보냅니다. |
            """))
    @ApiResponse(responseCode = "202", description = "PENDING: 처리 중. Retry-After 이후 GET /translations/{translationId}로 조회합니다.")
    @PostMapping("/v2/chatroom/lost-item/{articleId}/{chatRoomId}/messages/{messageId}/translations")
    ResponseEntity<TranslationResponse<TranslationData.Message>> lostItemMessage(
        @PathVariable Integer articleId, @PathVariable Integer chatRoomId,
        @Parameter(description = "원문 응답의 문자열 message_id. JavaScript Number로 변환하지 않습니다.",
            schema = @Schema(type = "string", implementation = String.class, pattern = "[0-9]+", example = "733900000000012345"))
        @PathVariable Long messageId,
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId, @RequestBody @Valid TranslationRequest request);

    @Operation(summary = "팀원 모집 쪽지 번역", description = "현재 방의 학생 멤버만 텍스트 쪽지를 번역할 수 있습니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = """
            | 필드 | 설명 |
            | --- | --- |
            | `target_language` | 번역할 언어 코드. `GET /languages`의 `code`를 사용합니다. |
            | `expected_source_version` | 원문 변경 확인용 버전. 팀원 모집 쪽지 조회 응답에서 번역할 메시지의 `translation_source_version`을 그대로 보냅니다. |
            """))
    @ApiResponse(responseCode = "202", description = "PENDING: 처리 중. Retry-After 이후 GET /translations/{translationId}로 조회합니다.")
    @PostMapping("/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}/messages/{messageId}/translations")
    ResponseEntity<TranslationResponse<TranslationData.Message>> recruitmentMessage(
        @PathVariable Integer recruitmentId, @PathVariable Integer chatRoomId, @PathVariable Integer messageId,
        @Auth(permit = {STUDENT}) Integer userId, @RequestBody @Valid TranslationRequest request);

    @Operation(summary = "번역 결과 조회",
        description = "`translation_id`로 처리 상태와 결과를 조회합니다. 쪽지는 현재 방 참여 권한이 필요합니다.")
    @ApiResponse(responseCode = "200", description = "SUCCESS: 완료 · PENDING: 처리 중 · FAILED: 실패. 재시도는 Retry-After 이후 원래 POST를 사용합니다.")
    @GetMapping("/translations/{translationId}")
    ResponseEntity<TranslationResponse<Map<String, Object>>> result(
        @PathVariable String translationId, @UserId Integer userId);
}
