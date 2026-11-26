package in.koreatech.koin.domain.dining.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.dining.dto.DiningResponse;
import in.koreatech.koin.domain.dining.dto.DiningSearchResponse;
import in.koreatech.koin.domain.dining.model.DiningPlace;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Dining: 식단", description = "식단 정보를 관리한다")
public interface DiningApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "식단 목록 조회", description = "language로 display_place를 선택합니다. type 표시명은 언어 카탈로그의 dining.type.{type}을 사용합니다. 식단 자동 번역은 조회 후 각 항목의 translation_source_version과 설정 언어로 POST /dinings/{id}/translations를 호출합니다. 원문 menu는 유지합니다.")
    @GetMapping("/dinings")
    ResponseEntity<List<DiningResponse>> getDinings(
        @DateTimeFormat(pattern = "yyMMdd")
        @Parameter(description = "조회 날짜(yyMMdd)") @RequestParam(required = false) LocalDate date,
        @UserId Integer userId,
        @RequestParam(defaultValue = "ko-KR") String language
    );

    @Operation(summary = "식단 좋아요")
    @PatchMapping("/dining/like")
    ResponseEntity<Void> likeDining(
        @Auth(permit = {GENERAL, STUDENT, COOP, COUNCIL}) Integer userId,
        @RequestParam Integer diningId
    );

    @Operation(summary = "식단 좋아요 취소")
    @PatchMapping("/dining/like/cancel")
    ResponseEntity<Void> likeDiningCancel(
        @Auth(permit = {GENERAL, STUDENT, COOP, COUNCIL}) Integer userId,
        @RequestParam Integer diningId
    );

    @Operation(summary = "영양사 월간조회 식단 검색")
    @GetMapping("/dinings/search")
    ResponseEntity<DiningSearchResponse> searchDinings(
        @Auth(permit = {COOP}) Integer userId,
        @RequestParam String keyword,
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @Parameter(description = "필터링 종류(A코너, B코너, C코너, 능수관, _2캠퍼스)") @RequestParam List<DiningPlace> filter
    );
}
