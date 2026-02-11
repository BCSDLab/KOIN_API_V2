package in.koreatech.koin.domain.callvan.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.callvan.dto.CallvanPostCreateRequest;
import in.koreatech.koin.domain.callvan.dto.CallvanPostCreateResponse;
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
}
