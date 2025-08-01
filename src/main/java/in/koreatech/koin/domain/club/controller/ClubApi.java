package in.koreatech.koin.domain.club.controller;

import static in.koreatech.koin.global.code.ApiResponseCode.*;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import in.koreatech.koin.global.code.ApiResponseCodes;
import in.koreatech.koin.domain.club.dto.request.ClubCreateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubEventCreateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubEventModifyRequest;
import in.koreatech.koin.domain.club.dto.request.ClubIntroductionUpdateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubManagerEmpowermentRequest;
import in.koreatech.koin.domain.club.dto.request.ClubQnaCreateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubRecruitmentCreateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubRecruitmentModifyRequest;
import in.koreatech.koin.domain.club.dto.request.ClubUpdateRequest;
import in.koreatech.koin.domain.club.dto.response.ClubEventResponse;
import in.koreatech.koin.domain.club.dto.response.ClubEventsResponse;
import in.koreatech.koin.domain.club.dto.response.ClubHotResponse;
import in.koreatech.koin.domain.club.dto.response.ClubQnasResponse;
import in.koreatech.koin.domain.club.dto.response.ClubRecruitmentResponse;
import in.koreatech.koin.domain.club.dto.response.ClubRelatedKeywordResponse;
import in.koreatech.koin.domain.club.dto.response.ClubResponse;
import in.koreatech.koin.domain.club.dto.response.ClubsByCategoryResponse;
import in.koreatech.koin.domain.club.enums.ClubEventType;
import in.koreatech.koin.domain.club.enums.ClubSortType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Club: 동아리", description = "동아리 정보를 관리한다")
@RequestMapping("/clubs")
public interface ClubApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리 생성을 요청한다")
    @PostMapping
    ResponseEntity<Void> createClubRequest(
        @RequestBody @Valid ClubCreateRequest clubCreateRequest,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리 정보를 수정한다")
    @PutMapping("/{clubId}")
    ResponseEntity<ClubResponse> updateClub(
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @RequestBody @Valid ClubUpdateRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리 상세정보를 수정한다")
    @PutMapping("/{clubId}/introduction")
    ResponseEntity<ClubResponse> updateClubIntroduction(
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @RequestBody @Valid ClubIntroductionUpdateRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리를 상세조회한다")
    @PostMapping("/{clubId}")
    ResponseEntity<ClubResponse> getClub(
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @UserId Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리를 조회한다", description = """
        - categoryId 값이 없으면 카테고리 구별없이 전체조회가 됩니다.
        - query에 내용을 넣으면 검색이 됩니다.
        - sortType의 기본값은 CREATED_AT_ASC (동아리 생성 순) 입니다.
        - isRecruiting의 기본값은 false 입니다.
        - RECRUITMENT_UPDATED_DESC, RECRUITING_DEADLINE_ASC은 isRecruiting가 true 경우에만 사용할 수 있습니다.
        
        ### 정렬 파라미터
        - NONE : 동아리 생성 순 (향후 정렬 조건 삭제)
        - CREATED_AT_ASC : 동아리 생성 순
        - HITS_DESC : 동아리 조회 순
        - RECRUITMENT_UPDATED_DESC : 동아리 모집 글 생성(수정)순
        - RECRUITING_DEADLINE_ASC : 동아리 모집 마감 순 (모집 마감 짧은 순 -> 상시 모집)
        
        ### 반환 정보
          - recruitmentInfo : 동아리 모집 정보를 담고 있습니다.
            - status : 동아리 모집 상태입니다.
              - NONE : 동아리 모집 글이 없는 상태입니다.
              - RECRUITING : 동아리 모집 중인 상태입니다.
              - CLOSED : 동아리 모집 글이 있는 상태에서 모집 마감된 상태입니다.
              - ALWAYS : 동아리 모집 글이 있는 상태에서 상시 모집중인 상태입니다.
            - Dday : 동아리 모집 디데이 입니다.
              - RECRUITING인 상태에서는 정수로 남은 일자가 내려갑니다.
              - 이외의 상태에서는 null로 내려갑니다.
        """)
    @GetMapping
    ResponseEntity<ClubsByCategoryResponse> getClubs(
        @RequestParam(required = false) Integer categoryId,
        @RequestParam(required = false, defaultValue = "false") Boolean isRecruiting,
        @RequestParam(required = false, defaultValue = "CREATED_AT_ASC") ClubSortType sortType,
        @RequestParam(required = false, defaultValue = "") String query,
        @UserId Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리 검색어에 따른 연관검색어 조회",
        description = """
            ### 접두어 연관검색어 조회
            - 검색어를 접두어로 가지는 모든 동아리명을 조회합니다.
            - ex) B -> BCSD, BASIA
            - 결과물로는 해당 동아리 ID와 동아리명을 반환합니다.
            - 최대 5개까지 반환합니다.
            - 공백, 대소문자 구분X
            - 클라측에서 입력지연(디바운스) 해주셔야 합니다. (무분별한 API 호출 방지)
            """
    )
    @GetMapping("/search/related")
    ResponseEntity<ClubRelatedKeywordResponse> getRelatedClubs(
        @RequestParam(required = false, defaultValue = "") String query
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "인기 동아리를 조회한다")
    @GetMapping("/hot")
    ResponseEntity<ClubHotResponse> getHotClub();

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리 좋아요를 누른다")
    @PutMapping("/{clubId}/like")
    ResponseEntity<Void> likeClub(
        @Auth(permit = {STUDENT}) Integer userId,
        @Parameter(in = PATH) @PathVariable Integer clubId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리 좋아요를 취소한다")
    @DeleteMapping("/{clubId}/like/cancel")
    ResponseEntity<Void> likeClubCancel(
        @Auth(permit = {STUDENT}) Integer userId,
        @Parameter(in = PATH) @PathVariable Integer clubId
    );

    @Operation(
        summary = "특정 동아리의 모든 QNA를 조회한다",
        description = """
            - authorId 확인하여 작성자 본인인 경우 삭제 버튼(x) 표시.
            - 닉네임은 존재 시 그대로 반환되며, 없는 경우 student의 익명 닉네임으로 반환.
            - 트리 구조는 대댓글 형태로 재귀적으로 구성됩니다.
            
            ```java
            예시
            댓글 1
            ├── 댓글 1-1
            │   ├── 댓글 1-1-1
            │   │   └── 댓글 1-1-1-1
            │   └── 댓글 1-1-2
            ├── 댓글 1-2
            └── 댓글 1-3
            
            댓글 2
            └── 댓글 2-1
                └── 댓글 2-1-1
            ```
            """
    )
    @GetMapping("/{clubId}/qna")
    ResponseEntity<ClubQnasResponse> getQnas(
        @Parameter(in = PATH) @PathVariable Integer clubId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 동아리의 QNA를 생성한다",
        description = """
            사용자의 경우 질문만 가능, 관리자의 경우 답변만 가능
            parentId를 null 요청 시 질문, 질문 QNA의 id를 넣어서 요청하면 답변 형식으로 생성
            """
    )
    @PostMapping("/{clubId}/qna")
    ResponseEntity<Void> createQna(
        @RequestBody @Valid ClubQnaCreateRequest request,
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "특정 동아리의 QNA를 삭제한다",
        description = """
            - 관리자는 모든 QNA 삭제 가능, 그 외에는 본인의 QNA만 삭제 가능
            - 부모 QNA(질문 QNA)인 경우, 답변 QNA까지 모두 삭제
            - 자식 QNA(답변 QNA)인 경우, 답변 QNA만 삭제
            """)
    @DeleteMapping("/{clubId}/qna/{qnaId}")
    ResponseEntity<Void> deleteQna(
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @Parameter(in = PATH) @PathVariable Integer qnaId,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "동아리 관리자 권한을 위임한다")
    @PutMapping("/empowerment")
    ResponseEntity<Void> empowermentClubManager(
        @RequestBody @Valid ClubManagerEmpowermentRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        OK,
        INVALID_RECRUITMENT_PERIOD,
        MUST_BE_NULL_RECRUITMENT_PERIOD,
        REQUIRED_RECRUITMENT_PERIOD,
        NOT_FOUND_CLUB,
        NOT_FOUND_USER,
        DUPLICATE_CLUB_RECRUITMENT,
        INVALID_REQUEST_BODY,
    })
    @Operation(summary = "동아리 모집 생성", description = """
        ### 동아리 모집 생성
        - 동아리 모집 생성을 합니다.
        - 모집 시작 기간과 모집 마감 기간은 "yyyy-MM-dd" 형식입니다.
        - 상시 모집 여부의 기본값은 false입니다.
        - 상시 모집 여부가 true인 경우, 모집 시작 기간과 모집 마감 기간은 null로 요청하셔야 합니다.
        """)
    @PostMapping("/{clubId}/recruitment")
    ResponseEntity<Void> createRecruitment(
        @RequestBody @Valid ClubRecruitmentCreateRequest request,
        @Parameter(description = "동아리 고유 식별자(clubId)", example = "1") @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        OK,
        INVALID_RECRUITMENT_PERIOD,
        MUST_BE_NULL_RECRUITMENT_PERIOD,
        REQUIRED_RECRUITMENT_PERIOD,
        NOT_FOUND_CLUB,
        NOT_FOUND_USER,
        NOT_FOUND_CLUB_RECRUITMENT,
        INVALID_REQUEST_BODY
    })
    @Operation(summary = "동아리 모집 수정", description = """
        ### 동아리 모집 수정
        - 동아리 모집을 수정 합니다.
        - 모집 시작 기간과 모집 마감 기간은 "yyyy-MM-dd" 형식입니다.
        - 상시 모집 여부가 true인 경우, 모집 시작 기간과 모집 마감 기간은 null로 요청하셔야 합니다.
        """)
    @PutMapping("/{clubId}/recruitment")
    ResponseEntity<Void> modifyRecruitment(
        @RequestBody @Valid ClubRecruitmentModifyRequest request,
        @Parameter(description = "동아리 고유 식별자(clubId)", example = "1") @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        NO_CONTENT,
        NOT_FOUND_CLUB,
        NOT_FOUND_USER,
        NOT_FOUND_CLUB_RECRUITMENT,
        INVALID_REQUEST_BODY
    })
    @Operation(summary = "동아리 모집 삭제", description = """
        ### 동아리 모집 수정
        - 동아리 모집을 삭제 합니다.
        """)
    @DeleteMapping("/{clubId}/recruitment")
    ResponseEntity<Void> deleteRecruitment(
        @Parameter(description = "동아리 고유 식별자(clubId)", example = "1") @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        OK,
        NOT_FOUND_CLUB,
        NOT_FOUND_CLUB_RECRUITMENT,
        INVALID_REQUEST_BODY
    })
    @Operation(summary = "동아리 모집 조회", description = """
        ### 동아리 모집 조회
        - 동아리 모집을 조회 합니다.
            - status : 동아리 모집 상태입니다.
              - NONE : 동아리 모집 글이 없는 상태입니다.
              - BEFORE : 동아리 모집 글이 있으며 모집 전 상태입니다.
              - RECRUITING : 동아리 모집 중인 상태입니다.
              - CLOSED : 동아리 모집 글이 있는 상태에서 모집 마감된 상태입니다.
              - ALWAYS : 동아리 모집 글이 있는 상태에서 상시 모집중인 상태입니다.
            - Dday : 동아리 모집 디데이 입니다.
              - RECRUITING인 상태에서는 정수로 남은 일자가 내려갑니다.
              - 이외의 상태에서는 null로 내려갑니다.
        """)
    @GetMapping("/{clubId}/recruitment")
    ResponseEntity<ClubRecruitmentResponse> getRecruitment(
        @Parameter(description = "동아리 고유 식별자(clubId)", example = "1") @PathVariable(name = "clubId") Integer clubId,
        @UserId Integer userId
    );

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
    @GetMapping("/{clubId}/event")
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

    @Operation(summary = "특정 동아리의 모집알림 구독")
    @ApiResponseCodes({
        CREATED,
        NOT_FOUND_USER,
        NOT_FOUND_CLUB,
        FORBIDDEN_USER_TYPE,
        INVALID_REQUEST_BODY
    })
    @PostMapping("{clubId}/recruitment/notification")
    ResponseEntity<Void> subscribeRecruitmentNotification(
        @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @Operation(summary = "특정 동아리의 모집알림 구독취소")
    @ApiResponseCodes({
        NO_CONTENT,
        NOT_FOUND_USER,
        NOT_FOUND_CLUB,
        FORBIDDEN_USER_TYPE,
        INVALID_REQUEST_BODY
    })
    @DeleteMapping("{clubId}/recruitment/notification")
    ResponseEntity<Void> rejectRecruitmentNotification(
        @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    );
}
