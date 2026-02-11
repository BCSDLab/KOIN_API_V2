package in.koreatech.koin.domain.callvan.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.callvan.dto.CallvanChatMessageRequest;
import in.koreatech.koin.domain.callvan.dto.CallvanChatMessageResponse;
import in.koreatech.koin.domain.callvan.dto.CallvanNotificationResponse;
import in.koreatech.koin.domain.callvan.dto.CallvanPostCreateRequest;
import in.koreatech.koin.domain.callvan.dto.CallvanPostCreateResponse;
import in.koreatech.koin.domain.callvan.dto.CallvanPostDetailResponse;
import in.koreatech.koin.domain.callvan.dto.CallvanPostSearchResponse;
import in.koreatech.koin.domain.callvan.model.enums.CallvanLocation;
import in.koreatech.koin.domain.callvan.model.filter.CallvanAuthorFilter;
import in.koreatech.koin.domain.callvan.model.filter.CallvanPostSortCriteria;
import in.koreatech.koin.domain.callvan.model.filter.CallvanPostStatusFilter;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Callvan: 콜밴", description = "콜밴 정보를 관리한다")
@RequestMapping("/callvan")
public interface CallvanApi {

    @ApiResponseCodes({
        CREATED,
        NOT_FOUND_USER,
        INVALID_REQUEST_BODY,
        INVALID_CUSTOM_LOCATION_NAME
    })
    @Operation(summary = "콜밴 게시글 생성", description = """
        ### 콜밴 게시글 생성 API
        새로운 콜밴 모집 게시글을 생성하고, 자동으로 관련 채팅방과 작성자를 참여자로 등록합니다.

        #### 인증 조건
        - **학생(STUDENT)** 권한을 가진 사용자만 호출 가능합니다.

        #### 요청 필드 설명
        - `departure_type`: 출발지 종류 (FRONT_GATE(정문), BACK_GATE(후문), TENNIS_COURT(테니스장), DORMITORY_MAIN(본관동), DORMITORY_SUB(별관동), TERMINAL(천안 터미널), STATION(천안역), ASAN_STATION(천안아산역), CUSTOM)
        - `departure_custom_name`: `departure_type`이 `CUSTOM`일 경우에만 사용자의 입력을 전달합니다. `CUSTOM`이 아닐 때 값을 전달(공백 포함)하거나 `CUSTOM`일때 값을 전달하지 않으면(null, 공백) 비즈니스 예외가 발생합니다.
        - `arrival_type`: 도착지 종류 (출발지와 동일한 옵션)
        - `arrival_custom_name`: `arrival_type`이 `CUSTOM`일 경우에만 사용자의 입력을 전달합니다. `CUSTOM`이 아닐 때 값을 전달(공백 포함)하거나 `CUSTOM`일때 값을 전달하지 않으면(null, 공백) 비즈니스 예외가 발생합니다.
        - `departure_date`: 출발 날짜 (`yyyy-MM-dd` 형식)
        - `departure_time`: 출발 시각 (`HH:mm` 형식, 24시간제)
        - `max_participants`: 모집할 최대 인원 (2~11명 사이만 가능)

        #### 비즈니스 로직
        1. `CUSTOM`이 아닌 장소 타입 선택 시, `custom_name` 필드는 비워두어야(null) 하며 서버에서 해당 타입의 명칭으로 자동 채워집니다.
        2. `CUSTOM`장소 타입 선택 시, `custom_name` 필드가 비워져있으면(null, blank) 오류가 발생합니다
        3. 게시글 생성 시 제목은 `{출발지} -> {도착지}` 형식으로 자동 생성됩니다.
        4. 생성과 동시에 해당 게시글 전용 채팅방(`CallvanChatRoom`)이 생성됩니다.
        5. 생성자는 자동으로 해당 게시글의 참여자(`CallvanParticipant`)로 등록되며, 역할은 `AUTHOR`로 설정됩니다.
        """)
    @PostMapping
    ResponseEntity<CallvanPostCreateResponse> createCallvanPost(
        @RequestBody @Valid CallvanPostCreateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    );

    @ApiResponseCodes({
        OK,
        NOT_FOUND_USER,
        UNAUTHORIZED_USER
    })
    @Operation(summary = "콜밴 게시글 목록 조회", description = """
        ### 콜밴 게시글 목록 조회 API
        여러 조건에 따라 콜밴 모집 게시글 목록을 조회합니다. 페이징과 정렬 기능이 포함되어 있습니다.

        #### 요청 필드 설명
        - `author`: 작성자 필터 (`ALL`(전체), `MY`(내 게시글)). `MY` 선택 시 인증이 필수입니다.
        - `status`: 모집 상태 필터 (`ALL`(전체), `RECRUITING`(모집 중), `CLOSED`(모집 마감), `COMPLETED`(완료)). 기본값은 `ALL`입니다.
        - `departures`: 출발지 필터 (`CallvanLocation` 목록). 여러 개 선택 가능합니다.
        - `departure_keyword`: 출발지 직접 입력 검색어. `departures`에 `CUSTOM`이 포함된 경우에만 유효하며, `departure_custom_name`에서 해당 검색어를 포함하는 게시글을 찾습니다.
        - `arrivals`: 도착지 필터. 출발지와 동일한 방식입니다.
        - `arrival_keyword`: 도착지 직접 입력 검색어.
        - `title`: 게시글 제목 검색어.
        - `sort`: 정렬 기준 (`LATEST`(최신순), `DEPARTURE`(출발 시간순)). 기본값은 `LATEST`입니다.
        - `page`: 페이지 번호 (1부터 시작, 기본값 1)
        - `limit`: 한 페이지당 게시글 수 (최대 50, 기본값 10)

        #### 비즈니스 로직
        1. 출발지/도착지 필터링 시, 선택된 장소 타입들에 해당하는 게시글을 조회합니다.
        2. `CUSTOM` 타입이 선택되고 키워드가 입력된 경우, 사용자가 직접 입력한 장소명에서 해당 키워드를 포함하는 게시글도 결과에 포함됩니다.
        3. 정렬 기준이 `DEPARTURE`인 경우, 출발 날짜와 시간 순으로 내림차순(최신 순) 정렬됩니다.
        4. 로그인된 사용자의 경우, 해당 콜벤 게시글에 합류한 상태면 `isJoined` 필드가 true로 표시됩니다.
        """)
    @GetMapping
    ResponseEntity<CallvanPostSearchResponse> getCallvanPosts(
        @RequestParam(required = false, defaultValue = "ALL") CallvanAuthorFilter author,
        @RequestParam(required = false, name = "departures") List<CallvanLocation> departures,
        @RequestParam(required = false, name = "departure_keyword") String departureKeyword,
        @RequestParam(required = false, name = "arrivals") List<CallvanLocation> arrivals,
        @RequestParam(required = false, name = "arrival_keyword") String arrivalKeyword,
        @RequestParam(required = false, defaultValue = "ALL") CallvanPostStatusFilter status,
        @RequestParam(required = false) String title,
        @RequestParam(required = false, defaultValue = "LATEST") CallvanPostSortCriteria sort,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @UserId Integer userId
    );

    @ApiResponseCodes({
        OK,
        NOT_FOUND_ARTICLE,
        FORBIDDEN_PARTICIPANT
    })
    @Operation(summary = "콜밴 게시글 상세 조회", description = """
        ### 콜밴 게시글 상세 조회 API
        콜밴 게시글의 상세 정보를 조회합니다. 참여자만 조회할 수 있습니다.

        #### 인증 조건
        - **학생(STUDENT)** 권한을 가진 사용자만 호출 가능합니다.
        - 해당 게시글의 참여자여야 합니다.

        #### 비즈니스 로직
        1. 존재하지 않는 게시글(`NOT_FOUND_ARTICLE`)이면 예외가 발생합니다.
        2. 참여자가 아닌 경우(`FORBIDDEN_PARTICIPANT`) 예외가 발생합니다.
        3. 참여자 목록(`participants`)에는 닉네임이 포함됩니다. (닉네임/익명닉네임 없을 시 랜덤 생성)
        """)
    @GetMapping("/posts/{postId}")
    ResponseEntity<CallvanPostDetailResponse> getCallvanPostDetail(
        @PathVariable Integer postId,
        @Auth(permit = {STUDENT}) Integer userId
    );

    @ApiResponseCodes({
        CREATED,
        NOT_FOUND_ARTICLE,
        CALLVAN_POST_NOT_RECRUITING,
        CALLVAN_POST_FULL,
        CALLVAN_ALREADY_JOINED
    })
    @Operation(summary = "콜밴 게시글 참여", description = """
        ### 콜밴 게시글 참여 API
        사용자가 특정 콜밴 게시글에 참여합니다.

        #### 인증 조건
        - **학생(STUDENT)** 권한을 가진 사용자만 호출 가능합니다.

        #### 비즈니스 로직
        1. 존재하지 않는 게시글(`NOT_FOUND_ARTICLE`)이면 예외가 발생합니다.
        2. 모집 중인 상태(`RECRUITING`)가 아니면(`CALLVAN_POST_NOT_RECRUITING`) 예외가 발생합니다.
        3. 이미 참여한 사용자이거나 작성자인 경우(`CALLVAN_ALREADY_JOINED`) 예외가 발생합니다.
        4. 모집 인원이 꽉 찬 경우(`CALLVAN_POST_FULL`) 예외가 발생합니다.
        5. 성공 시 참여자로 등록되고, 현재 모집 인원이 1 증가합니다.
        6. 참여로 인해 모집 인원이 가득 차면, 게시글 상태가 자동으로 `CLOSED`로 변경됩니다.
        """)
    @PostMapping("/posts/{postId}/participants")
    ResponseEntity<Void> joinCallvanPost(
        @PathVariable Integer postId,
        @Auth(permit = {STUDENT}) Integer userId
    );

    @ApiResponseCodes({
        NO_CONTENT,
        NOT_FOUND_ARTICLE,
        FORBIDDEN_AUTHOR
    })
    @Operation(summary = "콜밴 게시글 마감", description = """
        ### 콜밴 게시글 마감 API
        콜밴 게시글 작성자가 모집을 마감합니다.

        #### 인증 조건
        - **학생(STUDENT)** 권한을 가진 사용자만 호출 가능합니다.
        - 게시글 작성자 본인이어야 합니다.

        #### 비즈니스 로직
        1. 존재하지 않는 게시글(`NOT_FOUND_ARTICLE`)이면 예외가 발생합니다.
        2. 작성자가 아닌 경우(`FORBIDDEN_AUTHOR`) 예외가 발생합니다.
        3. 게시글 상태가 `CLOSED`로 변경됩니다.
        """)
    @PutMapping("/posts/{postId}/close")
    ResponseEntity<Void> closeCallvanPost(
        @PathVariable Integer postId,
        @Auth(permit = {STUDENT}) Integer userId
    );

    @ApiResponseCodes({
        NO_CONTENT,
        NOT_FOUND_ARTICLE,
        FORBIDDEN_AUTHOR,
        CALLVAN_POST_REOPEN_FAILED_FULL,
        CALLVAN_POST_REOPEN_FAILED_TIME
    })
    @Operation(summary = "콜밴 게시글 재모집", description = """
        ### 콜밴 게시글 재모집 API
        콜밴 게시글 작성자가 마감된 게시글을 다시 모집 상태로 변경합니다.

        #### 인증 조건
        - **학생(STUDENT)** 권한을 가진 사용자만 호출 가능합니다.
        - 게시글 작성자 본인이어야 합니다.

        #### 비즈니스 로직
        1. 존재하지 않는 게시글(`NOT_FOUND_ARTICLE`)이면 예외가 발생합니다.
        2. 작성자가 아닌 경우(`FORBIDDEN_AUTHOR`) 예외가 발생합니다.
        3. 인원이 가득 찬 경우(`CALLVAN_POST_REOPEN_FAILED_FULL`) 예외가 발생합니다.
        4. 출발 시간이 이미 지난 경우(`CALLVAN_POST_REOPEN_FAILED_TIME`) 예외가 발생합니다.
        5. 게시글 상태가 `RECRUITING`으로 변경됩니다.
        """)
    @PutMapping("/posts/{postId}/reopen")
    ResponseEntity<Void> reopenCallvanPost(
        @PathVariable Integer postId,
        @Auth(permit = {STUDENT}) Integer userId
    );

    @ApiResponseCodes({
        NO_CONTENT,
        NOT_FOUND_ARTICLE,
        FORBIDDEN_AUTHOR
    })
    @Operation(summary = "콜밴 게시글 완료", description = """
        ### 콜밴 게시글 완료 API
        콜밴 게시글 작성자가 모집을 완료합니다.

        #### 인증 조건
        - **학생(STUDENT)** 권한을 가진 사용자만 호출 가능합니다.
        - 게시글 작성자 본인이어야 합니다.

        #### 비즈니스 로직
        1. 존재하지 않는 게시글(`NOT_FOUND_ARTICLE`)이면 예외가 발생합니다.
        2. 작성자가 아닌 경우(`FORBIDDEN_AUTHOR`) 예외가 발생합니다.
        3. 게시글 상태가 `COMPLETED`로 변경됩니다.
        4. 마감 상태(`CLOSED`) 인 게시글만 `COMPLETED`로 변경됩니다.
        """)
    @PutMapping("/posts/{postId}/complete")
    ResponseEntity<Void> completeCallvanPost(
        @PathVariable Integer postId,
        @Auth(permit = {STUDENT}) Integer userId
    );

    @ApiResponseCodes({
        NO_CONTENT,
        NOT_FOUND_ARTICLE,
        FORBIDDEN_PARTICIPANT,
        CALLVAN_POST_AUTHOR
    })
    @Operation(summary = "콜밴 나가기", description = """
        ### 콜밴 게시글 탈퇴 API
        사용자가 참여한 콜밴 게시글에서 탈퇴합니다.

        #### 인증 조건
        - **학생(STUDENT)** 권한을 가진 사용자만 호출 가능합니다.

        #### 비즈니스 로직
        1. 존재하지 않는 게시글(`NOT_FOUND_ARTICLE`)이면 예외가 발생합니다.
        2. 참여자가 아닌 경우(`FORBIDDEN_PARTICIPANT`) 예외가 발생합니다.
        3. 탈퇴 시 참여자 목록에서 삭제되고, 현재 모집 인원이 1 감소합니다.
        4. 채팅 내역에서 해당 사용자는 '나감' 상태로 표시됩니다.
        5. 콜벤 게시글 작성자는 나갈 수 없습니다.
        """)
    @DeleteMapping("/posts/{postId}/participants")
    ResponseEntity<Void> leaveCallvanPost(
        @PathVariable Integer postId,
        @Auth(permit = {STUDENT}) Integer userId
    );

    @ApiResponseCodes({
        OK,
        NOT_FOUND_ARTICLE,
        FORBIDDEN_PARTICIPANT
    })
    @Operation(summary = "콜밴 게시글 채팅 조회", description = """
        ### 콜밴 게시글 채팅 조회 API
        콜밴 게시글의 채팅 내역을 조회합니다.

        #### 인증 조건
        - **학생(STUDENT)** 권한을 가진 사용자만 호출 가능합니다.
        - 해당 콜벤의 참여자여야 합니다.

        #### 비즈니스 로직
        1. 존재하지 않는 게시글(`NOT_FOUND_ARTICLE`)이면 예외가 발생합니다.
        2. 참여자가 아닌 경우(`FORBIDDEN_PARTICIPANT`) 예외가 발생합니다.
        3. 채팅 내역은 오래된 순으로 정렬되어 반환됩니다.
        """)
    @GetMapping("/posts/{postId}/chat")
    ResponseEntity<CallvanChatMessageResponse> getCallvanChatMessages(
        @PathVariable Integer postId,
        @Auth(permit = {STUDENT}) Integer userId
    );

    @ApiResponseCodes({
        CREATED,
        NOT_FOUND_ARTICLE,
        FORBIDDEN_PARTICIPANT
    })
    @Operation(summary = "콜밴 게시글 채팅 전송", description = """
        ### 콜밴 게시글 채팅 전송 API
        콜밴 게시글 채팅방에 메시지를 전송합니다.

        #### 인증 조건
        - **학생(STUDENT)** 권한을 가진 사용자만 호출 가능합니다.
        - 해당 콜벤의 참여자여야 합니다.

        #### 비즈니스 로직
        1. 존재하지 않는 게시글(`NOT_FOUND_ARTICLE`)이면 예외가 발생합니다.
        2. 참여자가 아닌 경우(`FORBIDDEN_PARTICIPANT`) 예외가 발생합니다.
        3. 이미지 전송 시 `is_image`를 true로 설정하고 `content`에 이미지 URL을 담아 보내야 합니다.
        """)
    @PostMapping("/posts/{postId}/chat")
    ResponseEntity<Void> sendMessage(
        @PathVariable Integer postId,
        @RequestBody @Valid CallvanChatMessageRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    );

    @ApiResponseCodes({
        OK
    })
    @Operation(summary = "콜밴 알림 목록 조회", description = """
        ### 콜밴 알림 목록 조회 API
        로그인한 사용자의 알림 목록을 최신순으로 조회합니다.

        ### 알림 타입별 데이터 구조
        | 필드명 | RECRUITMENT_COMPLETE(인원 모집 완료) | NEW_MESSAGE(신규 채팅 도착) | PARTICIPANT_JOINED(신규 인원 참여) | DEPARTURE_UPCOMING(출발 30분 전) |
        | :--- | :--- | :--- | :--- | :--- |
        | type | RECRUITMENT_COMPLETE | NEW_MESSAGE | PARTICIPANT_JOINED | DEPARTURE_UPCOMING |
        | message_preview | "해당 콜벤팟 인원이 모두 모집되었습니다. 콜벤을 예약하세요" | 신규 채팅 메시지 내용 | null | null |
        | sender_nickname | null | 발신자 닉네임 | null | null |
        | joined_member_nickname | null | null | 참여자 닉네임 | null |
        | post_id | 게시글 ID | 게시글 ID | 게시글 ID | 게시글 ID |
        | departure | 출발지 | 출발지 | 출발지 | 출발지 |
        | arrival | 도착지 | 도착지 | 도착지 | 도착지 |
        | departure_date | 출발 날짜 | 출발 날짜 | 출발 날짜 | 출발 날짜 |
        | departure_time | 출발 시간 | 출발 시간 | 출발 시간 | 출발 시간 |
        | current_participants | 현재 인원 | 현재 인원 | 현재 인원 | 현재 인원 |
        | max_participants | 최대 인원 | 최대 인원 | 최대 인원 | 최대 인원 |
        """)
    @GetMapping("/notifications")
    ResponseEntity<List<CallvanNotificationResponse>> getNotifications(
        @Auth(permit = {STUDENT}) Integer userId
    );

    @ApiResponseCodes({
        NO_CONTENT
    })
    @Operation(summary = "콜밴 알림 모두 읽음 처리", description = """
        ### 콜밴 알림 모두 읽음 처리 API
        로그인한 사용자의 모든 알림을 읽음 처리합니다.
        """)
    @PostMapping("/notifications/mark-all-read")
    ResponseEntity<Void> markAllNotificationsAsRead(
        @Auth(permit = {STUDENT}) Integer userId
    );
}
