package in.koreatech.koin.domain.club.event.controller;

import in.koreatech.koin.domain.club.event.dto.request.ClubEventCreateRequest;
import in.koreatech.koin.domain.club.event.dto.request.ClubEventModifyRequest;
import in.koreatech.koin.domain.club.event.dto.response.ClubEventResponse;
import in.koreatech.koin.domain.club.event.dto.response.ClubEventsResponse;
import in.koreatech.koin.domain.club.event.enums.ClubEventType;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

@Tag(name = "(Normal) Club Event: 동아리 행사", description = "동아리 행사 정보를 관리한다")
@RequestMapping("/clubs")
public interface ClubEventApi {

    @ApiResponseCodes({
        OK,
        INVALID_CLUB_EVENT_PERIOD,
        NOT_FOUND_CLUB,
        NOT_FOUND_USER,
        INVALID_REQUEST_BODY
    })
    @Operation(summary = "동아리 행사를 생성한다", description = """
        ### 동아리 행사 생성
        - 동아리 행사를 생성합니다.
        - 모집 시작 기간과 모집 마감 기간은 "yyyy-MM-ddT00:00:00" 형식입니다.
        - ex) "2025-07-02T18:00:00"
        - 기본적으로 "yyyy-MM-ddT00:00:00.000z" 형식으로 Swagger에서 반환되어, 뒤의 microsec를 지워야합니다.
        """)
    @PostMapping("/{clubId}/event")
    ResponseEntity<Void> createClubEvent(
        @PathVariable Integer clubId,
        @RequestBody @Valid ClubEventCreateRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        OK,
        INVALID_CLUB_EVENT_PERIOD,
        NOT_FOUND_CLUB,
        NOT_FOUND_USER,
        NOT_FOUND_CLUB_EVENT,
        INVALID_REQUEST_BODY
    })
    @Operation(summary = "동아리 행사를 수정한다", description = """
        ### 동아리 행사 수정
        - 동아리 행사를 수정합니다.
        - 모집 시작 기간과 모집 마감 기간은 "yyyy-MM-ddT00:00:00" 형식입니다.
        - ex) "2025-07-02T18:00:00"
        - 기본적으로 "yyyy-MM-ddT00:00:00.000z" 형식으로 Swagger에서 반환되어, 뒤의 microsec를 지워야합니다.
        """)
    @PutMapping("/{clubId}/event/{eventId}")
    ResponseEntity<Void> modifyClubEvent(
        @PathVariable Integer clubId,
        @PathVariable Integer eventId,
        @RequestBody @Valid ClubEventModifyRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        NO_CONTENT,
        NOT_FOUND_CLUB,
        NOT_FOUND_USER,
        NOT_FOUND_CLUB_EVENT,
        INVALID_REQUEST_BODY
    })
    @Operation(summary = "동아리 행사를 삭제한다", description = """
        ### 동아리 행사 삭제
        - 동아리 행사를 삭제합니다.
        """)
    @DeleteMapping("/{clubId}/event/{eventId}")
    ResponseEntity<Void> deleteClubEvent(
        @PathVariable Integer clubId,
        @PathVariable Integer eventId,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        OK,
        NOT_FOUND_CLUB,
        NOT_FOUND_CLUB_EVENT,
        INVALID_REQUEST_BODY
    })
    @Operation(summary = "동아리 행사를 단일 조회한다.", description = """
        ### 동아리 행사 단일 조회
        - 동아리 행사를 단일 조회합니다.
        
        ### 응답값
        - status
            - SOON : 곧 행사 진행
            - ONGOING : 행사 진행 중
            - UPCOMING : 행사 예정
            - ENDED : 종료된 행사
        """)
    @GetMapping("/{clubId}/event/{eventId}")
    ResponseEntity<ClubEventResponse> getClubEvent(
        @PathVariable Integer clubId,
        @PathVariable Integer eventId
    );

    @ApiResponseCodes({
        OK,
        INVALID_CLUB_EVENT_TYPE,
        NOT_FOUND_CLUB,
        NOT_FOUND_CLUB_EVENT,
        INVALID_REQUEST_BODY
    })
    @Operation(summary = "동아리 행사를 상태에 따라 조회한다", description = """
        ### 동아리 행사 전체 조회
        - 동아리 행사를 상태에 따라 조회합니다.
        - default 조회값은 RECENT입니다. 기본 정렬 값은 최신 등록순입니다.
        - eventType
            - RECENT : 최신 등록순으로 조회됩니다. 종료된 행사는 가장 아래에 깔립니다. 그 외는 최신 등록순입니다.
            - ONGOING : 행사 시작 1시간 전과 진행 중인 행사가 조회됩니다.
            - UPCOMING : 행사 시작 시간이 1시간 이상인 행사가 조회됩니다.
            - ENDED : 행사가 종료되고 1분이 지난 시점의 행사가 조회됩니다.
        
        ### 응답값
        - status
            - SOON : 곧 행사 진행
            - ONGOING : 행사 진행 중
            - UPCOMING : 행사 예정
            - ENDED : 종료된 행사
        """)
    @GetMapping("/{clubId}/events")
    ResponseEntity<List<ClubEventsResponse>> getClubEvents(
        @PathVariable Integer clubId,
        @RequestParam(defaultValue = "RECENT") ClubEventType eventType,
        @UserId Integer userId
    );

    @Operation(summary = "특정 동아리의 특정 행사알림 구독")
    @ApiResponseCodes({
        CREATED,
        NOT_FOUND_USER,
        NOT_FOUND_CLUB,
        NOT_FOUND_CLUB_EVENT,
        FORBIDDEN_USER_TYPE,
        NOT_MATCHED_CLUB_AND_EVENT,
        INVALID_REQUEST_BODY
    })
    @PostMapping("{clubId}/event/{eventId}/notification")
    ResponseEntity<Void> subscribeEventNotification(
        @PathVariable Integer clubId,
        @PathVariable Integer eventId,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @Operation(summary = "특정 동아리의 특정 행사알림 구독취소")
    @ApiResponseCodes({
        NO_CONTENT,
        NOT_FOUND_USER,
        NOT_FOUND_CLUB,
        NOT_FOUND_CLUB_EVENT,
        FORBIDDEN_USER_TYPE,
        NOT_MATCHED_CLUB_AND_EVENT,
        INVALID_REQUEST_BODY
    })
    @DeleteMapping("{clubId}/event/{eventId}/notification")
    ResponseEntity<Void> rejectEventNotification(
        @PathVariable Integer clubId,
        @PathVariable Integer eventId,
        @Auth(permit = {STUDENT}) Integer studentId
    );
}
