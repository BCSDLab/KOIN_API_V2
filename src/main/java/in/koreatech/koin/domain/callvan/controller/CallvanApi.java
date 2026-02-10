package in.koreatech.koin.domain.callvan.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.callvan.dto.CallvanPostCreateRequest;
import in.koreatech.koin.domain.callvan.dto.CallvanPostCreateResponse;
import in.koreatech.koin.global.auth.Auth;
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
}
