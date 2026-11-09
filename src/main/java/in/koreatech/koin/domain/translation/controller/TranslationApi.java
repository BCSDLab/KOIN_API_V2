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
    @ApiResponse(responseCode = "409", description = "SOURCE_CHANGED: 원문을 다시 조회해 화면을 갱신하세요. 새 translation_source_version을 expected_source_version에 넣어 번역 POST를 다시 요청하세요.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public interface TranslationApi {
    @Operation(summary = "상점 메뉴 분류 이름 번역", description = "분류 이름을 번역합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = """
            | 필드 | 설명 |
            | --- | --- |
            | `target_language` | 대상 언어. `GET /languages`의 코드를 사용합니다. |
            | `expected_source_version` | 메뉴 분류 GET의 해당 `menu_categories[]` 항목에서 `translation_source_version`을 그대로 복사해 보냅니다. |

            409가 오면 원문을 다시 조회해 화면을 갱신하고, 새 버전으로 번역 POST를 다시 요청합니다.
            """))
    @SecurityRequirements
    @ApiResponse(responseCode = "202", description = "PENDING: Retry-After 이후 공통 번역 결과 API로 조회합니다.")
    @PostMapping("/shops/{shopId}/menus/categories/{id}/translations")
    ResponseEntity<TranslationResponse<TranslationData.Name>> shopMenuCategory(
        @PathVariable Integer shopId, @PathVariable Integer id, @RequestBody @Valid TranslationRequest request);

    @Operation(summary = "주문 메뉴 분류 이름 번역", description = "주문 메뉴의 분류 이름을 번역합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = """
            | 필드 | 설명 |
            | --- | --- |
            | `target_language` | 대상 언어. `GET /languages`의 코드를 사용합니다. |
            | `expected_source_version` | 주문 메뉴 분류 GET의 해당 `menu_groups[]` 항목에서 `translation_source_version`을 그대로 복사해 보냅니다. |

            409가 오면 원문을 다시 조회해 화면을 갱신하고, 새 버전으로 번역 POST를 다시 요청합니다.
            """))
    @SecurityRequirements
    @ApiResponse(responseCode = "202", description = "PENDING: Retry-After 이후 공통 번역 결과 API로 조회합니다.")
    @PostMapping("/order/shop/{orderableShopId}/menus/groups/{id}/translations")
    ResponseEntity<TranslationResponse<TranslationData.Name>> orderableShopMenuGroup(
        @PathVariable Integer orderableShopId, @PathVariable Integer id, @RequestBody @Valid TranslationRequest request);

    @Operation(summary = "주문 메뉴·옵션 번역", description = "메뉴와 옵션의 이름·설명을 번역합니다. 항목 ID는 원문과 같습니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = """
            | 필드 | 설명 |
            | --- | --- |
            | `target_language` | 대상 언어. `GET /languages`의 코드를 사용합니다. |
            | `expected_source_version` | 주문 메뉴 상세 GET 응답의 `translation_source_version`을 그대로 복사해 보냅니다. |

            409가 오면 원문을 다시 조회해 화면을 갱신하고, 새 버전으로 번역 POST를 다시 요청합니다.
            """))
    @SecurityRequirements
    @ApiResponse(responseCode = "202", description = "PENDING: Retry-After 이후 공통 번역 결과 API로 조회합니다.")
    @PostMapping("/order/shop/{orderableShopId}/menus/{orderableShopMenuId}/translations")
    ResponseEntity<TranslationResponse<TranslationData.OrderableShopMenu>> orderableShopMenu(
        @PathVariable Integer orderableShopId, @PathVariable Integer orderableShopMenuId,
        @RequestBody @Valid TranslationRequest request);

    @Operation(summary = "상점 소개·공지 번역", description = "상점 소개·공지·상세 위치 설명을 번역합니다. 경로 ID에는 주문상점 상세의 shop_id를 사용합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = """
            | 필드 | 설명 |
            | --- | --- |
            | `target_language` | 대상 언어. `GET /languages`의 코드를 사용합니다. |
            | `expected_source_version` | `GET /order/shop/{orderableShopId}/detail`의 `translation_source_version`을 그대로 복사해 보냅니다. |

            409가 오면 원문을 다시 조회해 화면을 갱신하고, 새 버전으로 번역 POST를 다시 요청합니다.
            """))
    @SecurityRequirements
    @ApiResponse(responseCode = "202", description = "PENDING: Retry-After 이후 공통 번역 결과 API로 조회합니다.")
    @PostMapping("/shops/{id}/info/translations")
    ResponseEntity<TranslationResponse<TranslationData.ShopInfo>> shopInfo(
        @PathVariable Integer id, @RequestBody @Valid TranslationRequest request);

    @Operation(summary = "식단 메뉴 번역", description = "식단의 메뉴를 원래 순서대로 번역합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = """
            | 필드 | 설명 |
            | --- | --- |
            | `target_language` | 대상 언어. `GET /languages`의 코드를 사용합니다. |
            | `expected_source_version` | `GET /dinings`에서 번역할 식단의 `translation_source_version`을 그대로 복사해 보냅니다. |

            409가 오면 원문을 다시 조회해 화면을 갱신하고, 새 버전으로 번역 POST를 다시 요청합니다.
            """))
    @SecurityRequirements
    @ApiResponse(responseCode = "202", description = "PENDING: Retry-After 이후 공통 번역 결과 API로 조회합니다.")
    @PostMapping("/dinings/{id}/translations")
    ResponseEntity<TranslationResponse<TranslationData.Dining>> dining(
        @PathVariable Integer id, @RequestBody @Valid TranslationRequest request);

    @Operation(summary = "상점 설명 번역", description = "상점의 설명을 번역합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = """
            | 필드 | 설명 |
            | --- | --- |
            | `target_language` | 대상 언어. `GET /languages`의 코드를 사용합니다. |
            | `expected_source_version` | 상점 상세 GET 응답의 `translation_source_version`을 그대로 복사해 보냅니다. |

            409가 오면 원문을 다시 조회해 화면을 갱신하고, 새 버전으로 번역 POST를 다시 요청합니다.
            """))
    @SecurityRequirements
    @ApiResponse(responseCode = "202", description = "PENDING: Retry-After 이후 공통 번역 결과 API로 조회합니다.")
    @PostMapping("/shops/{id}/translations")
    ResponseEntity<TranslationResponse<TranslationData.Shop>> shop(
        @PathVariable Integer id, @RequestBody @Valid TranslationRequest request);

    @Operation(summary = "상점 메뉴 번역", description = "상점 메뉴의 이름·설명·옵션명을 번역합니다. 가격과 옵션 순서는 유지합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = """
            | 필드 | 설명 |
            | --- | --- |
            | `target_language` | 대상 언어. `GET /languages`의 코드를 사용합니다. |
            | `expected_source_version` | 메뉴 목록의 해당 메뉴 또는 메뉴 상세 응답의 `translation_source_version`을 그대로 복사해 보냅니다. |

            409가 오면 원문을 다시 조회해 화면을 갱신하고, 새 버전으로 번역 POST를 다시 요청합니다.
            """))
    @SecurityRequirements
    @ApiResponse(responseCode = "202", description = "PENDING: Retry-After 이후 공통 번역 결과 API로 조회합니다.")
    @PostMapping("/shops/{shopId}/menus/{id}/translations")
    ResponseEntity<TranslationResponse<TranslationData.ShopMenu>> shopMenu(
        @PathVariable Integer shopId, @PathVariable Integer id, @RequestBody @Valid TranslationRequest request);

    @Operation(summary = "공지 AI 요약 번역", description = "현재 공지의 AI 요약을 번역합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = """
            | 필드 | 설명 |
            | --- | --- |
            | `target_language` | 번역할 언어 코드. `GET /languages`의 `code`를 사용합니다. |
            | `expected_source_version` | 공지 조회 응답의 `ai_summary.translation_source_version`을 그대로 복사해 보냅니다. |

            409가 오면 원문을 다시 조회해 화면을 갱신하고, 새 버전으로 번역 POST를 다시 요청합니다.
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
            | `expected_source_version` | 분실물 조회 응답의 `translation_source_version`을 그대로 복사해 보냅니다. |

            409가 오면 원문을 다시 조회해 화면을 갱신하고, 새 버전으로 번역 POST를 다시 요청합니다.
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
            | `expected_source_version` | 모집글 조회 응답의 `translation_source_version`을 그대로 복사해 보냅니다. |

            409가 오면 원문을 다시 조회해 화면을 갱신하고, 새 버전으로 번역 POST를 다시 요청합니다.
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
            | `expected_source_version` | 분실물 쪽지 조회 응답에서 번역할 메시지의 `translation_source_version`을 그대로 복사해 보냅니다. |

            409가 오면 원문을 다시 조회해 화면을 갱신하고, 새 버전으로 번역 POST를 다시 요청합니다.
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
            | `expected_source_version` | 팀원 모집 쪽지 조회 응답에서 번역할 메시지의 `translation_source_version`을 그대로 복사해 보냅니다. |

            409가 오면 원문을 다시 조회해 화면을 갱신하고, 새 버전으로 번역 POST를 다시 요청합니다.
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
