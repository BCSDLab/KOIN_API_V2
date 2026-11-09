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

@Tag(name = "(Normal) Translation: 번역", description = "버튼으로 요청한 원문만 Solar로 번역하고 저장·재사용합니다.")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "완료 또는 실패·제공 불가 결과. status와 reason_code를 확인합니다."),
    @ApiResponse(responseCode = "400", description = "필수값·언어 코드·원문 버전 형식 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "401", description = "쪽지 번역에 본인 인증이 필요합니다.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "403", description = "현재 접근 권한 없음 또는 쿠키 Origin/CSRF 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "원문·방·메시지·결과가 없거나 결과 보관 기간이 지났습니다.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "409", description = "SOURCE_CHANGED: 원문을 다시 조회해 주세요.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public interface TranslationApi {
    String FLOW = """
        현재 화면 언어와 원문 응답의 translation_source_version을 전달합니다. 원문 텍스트는 서버에서 읽습니다.
        저장된 결과는 즉시 재사용하고, 새 번역은 요청 안에서 최대 25초(기본값) 동안 Solar를 호출합니다.
        같은 원문·언어가 이미 처리 중이면 202/PENDING과 Retry-After를 반환합니다.
        성공은 SUCCESS, 지원하지 않는 대상은 UNAVAILABLE, AI 오류·호출 제한은 FAILED입니다.
        원문은 변경하지 않습니다. 실패·제공 불가 시 원문을 유지하고 서로 다른 안내를 표시합니다.
        실패는 Retry-After 이후 POST로 재시도합니다. 원문 수정·삭제·접근 종료는 저장 결과에도 적용됩니다.
        """;

    @Operation(summary = "공지 AI 요약 번역", description = FLOW + """
        공지 본문·제목을 번역하지 않습니다. ai_summary.translation_source_version을 사용합니다.
        현재 표시되는 요약 text만 번역하며 아이콘·순서·항목 수를 보존합니다.
        한국어 요약의 목표 언어가 ko-KR이면 Solar 호출 없이 원문을 반환하며 translation_id는 null입니다.
        외국어 번역에 한국어 요약의 100자 생성 상한을 적용하지 않습니다.
        """)
    @SecurityRequirements
    @ApiResponse(responseCode = "202", description = "동일한 번역 처리 중. 결과 GET으로 확인합니다.")
    @PostMapping("/v2/articles/{id}/ai-summary/translations")
    ResponseEntity<TranslationResponse<TranslationData.Summary>> summary(
        @PathVariable Integer id, @RequestBody @Valid TranslationRequest request);

    @Operation(summary = "분실물 게시글 번역", description = FLOW + """
        found_place, content를 번역합니다. category·날짜·작성자·이미지는 원문 응답을 사용합니다.
        """)
    @SecurityRequirements
    @ApiResponse(responseCode = "202", description = "동일한 번역 처리 중")
    @PostMapping("/articles/lost-item/{id}/translations")
    ResponseEntity<TranslationResponse<TranslationData.LostItem>> lostItem(
        @PathVariable Integer id, @RequestBody @Valid TranslationRequest request);

    @Operation(summary = "팀원 모집글 번역", description = FLOW + """
        title, description, qualification을 번역합니다. null·빈 원문 필드는 그대로 유지합니다.
        """)
    @SecurityRequirements
    @ApiResponse(responseCode = "202", description = "동일한 번역 처리 중")
    @PostMapping("/team-recruitments/{id}/translations")
    ResponseEntity<TranslationResponse<TranslationData.Recruitment>> recruitment(
        @PathVariable Integer id, @RequestBody @Valid TranslationRequest request);

    @Operation(summary = "분실물 쪽지 번역", description = FLOW + """
        일반·학생·총학생회 중 방 참여자만 사용할 수 있습니다. 양방향 차단과 방 진입 가능 여부를 확인합니다.
        messageId는 응답의 문자열 message_id를 사용합니다. 이미지·이모지 전용 메시지는 미지원입니다.
        번역만으로 읽음·접속 상태를 변경하지 않습니다.
        """)
    @ApiResponse(responseCode = "202", description = "동일한 번역 처리 중")
    @PostMapping("/v2/chatroom/lost-item/{articleId}/{chatRoomId}/messages/{messageId}/translations")
    ResponseEntity<TranslationResponse<TranslationData.Message>> lostItemMessage(
        @PathVariable Integer articleId, @PathVariable Integer chatRoomId,
        @Parameter(description = "원문 응답의 문자열 message_id. JavaScript Number로 변환하지 않습니다.",
            schema = @Schema(type = "string", implementation = String.class, pattern = "[0-9]+", example = "733900000000012345"))
        @PathVariable Long messageId,
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId, @RequestBody @Valid TranslationRequest request);

    @Operation(summary = "팀원 모집 쪽지 번역", description = FLOW + """
        학생 계정의 현재 방 멤버만 사용할 수 있습니다. 모집 마감·삭제, 발신자 탈퇴 후에도
        기존 읽기 권한이 남은 메시지는 번역할 수 있습니다. 이미지·이모지 전용은 미지원입니다.
        번역만으로 읽음·접속 상태를 변경하지 않습니다.
        """)
    @ApiResponse(responseCode = "202", description = "동일한 번역 처리 중")
    @PostMapping("/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}/messages/{messageId}/translations")
    ResponseEntity<TranslationResponse<TranslationData.Message>> recruitmentMessage(
        @PathVariable Integer recruitmentId, @PathVariable Integer chatRoomId, @PathVariable Integer messageId,
        @Auth(permit = {STUDENT}) Integer userId, @RequestBody @Valid TranslationRequest request);

    @Operation(summary = "번역 결과 조회", description = """
        POST가 반환한 translation_id로 현재 결과를 조회합니다. GET은 Solar 호출이나 재시도를 시작하지 않습니다.
        공개 게시글 결과는 비로그인 조회 가능하고, 쪽지는 원문과 같은 계정 유형·방 참여 권한이 필요합니다.
        매번 현재 원문·접근 권한·버전을 확인하므로 결과 ID만 알아도 읽을 수 있는 것은 아닙니다.
        처리 중에는 200/PENDING과 Retry-After: 2, 중단된 처리의 선점 만료 후에는 FAILED/TEMPORARY_ERROR를 반환합니다.
        실패는 Retry-After 이후 원래 POST로 재시도합니다. 만료 결과는 404입니다.
        SUCCESS의 data 형식은 요청했던 요약·분실물·모집글·메시지 POST 응답과 같습니다.
        """)
    @GetMapping("/translations/{translationId}")
    ResponseEntity<TranslationResponse<Map<String, Object>>> result(
        @PathVariable String translationId, @UserId Integer userId);
}
