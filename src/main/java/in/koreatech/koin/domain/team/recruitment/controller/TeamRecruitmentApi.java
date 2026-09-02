package in.koreatech.koin.domain.team.recruitment.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static in.koreatech.koin.global.code.ApiResponseCode.CREATED;
import static in.koreatech.koin.global.code.ApiResponseCode.ILLEGAL_ARGUMENT;
import static in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_USER_TYPE;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_START_DATE_AFTER_END_DATE;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_USER;
import static in.koreatech.koin.global.code.ApiResponseCode.NO_CONTENT;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CLOSED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_FORBIDDEN;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_INVALID_DEADLINE_DATE;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_INVALID_ROLE_COMPOSITION;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_MAX_PARTICIPANTS_BELOW_ACCEPTED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_NOT_FOUND;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_ROLE_NOT_FOUND;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_ROLE_UPDATE_NOT_ALLOWED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_TYPE_CHANGE_NOT_ALLOWED;
import static in.koreatech.koin.global.code.ApiResponseCode.UNAUTHORIZED_USER;

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

import in.koreatech.koin.domain.team.recruitment.dto.CreateRecruitmentRequest;
import in.koreatech.koin.domain.team.recruitment.dto.CreatedRecruitmentListResponse;
import in.koreatech.koin.domain.team.recruitment.dto.IdResponse;
import in.koreatech.koin.domain.team.recruitment.dto.RecruitmentDetail;
import in.koreatech.koin.domain.team.recruitment.dto.RecruitmentListResponse;
import in.koreatech.koin.domain.team.recruitment.dto.UpdateRecruitmentRequest;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentSort;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatusFilter;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Team Recruitment Articles: 팀원 모집글", description = "팀원 모집글을 관리한다")
@RequestMapping("/team-recruitments")
public interface TeamRecruitmentApi {

    @ApiResponseCodes({
        OK,
        ILLEGAL_ARGUMENT,
    })
    @Operation(summary = "모집글 목록 조회", description = """
        ### 모집글 목록 조회
        - 비로그인 조회가 가능합니다.
        - `keyword`는 모집글 제목, 역할명, 모집 카테고리 표시명, 진행 방식 표시명을
          대소문자 구분 없이 부분 일치로 검색합니다. 본문은 검색 대상이 아닙니다.
        - `status`는 ALL 전체, RECRUITING 모집 중, CLOSED 모집 마감입니다. 삭제된 모집글은 항상 제외됩니다.
        - `categories`는 복수 지정이 가능하며, `meetingType`은 전체 조회 시 파라미터를 생략하셔야 합니다.
        - `sort`는 LATEST_DESC 최신순, DEADLINE_ASC 마감 임박순입니다.
        - `page`는 1보다 작으면 1로, 전체 페이지를 넘으면 마지막 페이지로 보정됩니다.
        - `limit`은 1보다 작으면 1로, 50보다 크면 50으로 보정됩니다.
        """)
    @GetMapping
    ResponseEntity<RecruitmentListResponse> getRecruitments(
        @Parameter(description = "검색어. 제목, 역할명, 카테고리/진행 방식 표시명을 검색합니다.", example = "공모전")
        @RequestParam(required = false) String keyword,
        @Parameter(description = "모집 상태 필터", example = "ALL")
        @RequestParam(required = false, defaultValue = "ALL") TeamRecruitmentStatusFilter status,
        @Parameter(description = "모집 카테고리 필터. 복수 지정이 가능합니다.", example = "CONTEST")
        @RequestParam(required = false) List<TeamRecruitmentCategory> categories,
        @Parameter(description = "진행 방식 필터", example = "ONLINE")
        @RequestParam(required = false) TeamRecruitmentMeetingType meetingType,
        @Parameter(description = "정렬 방식", example = "LATEST_DESC")
        @RequestParam(required = false, defaultValue = "LATEST_DESC") TeamRecruitmentSort sort,
        @Parameter(description = "페이지", example = "1")
        @RequestParam(required = false, defaultValue = "1") Integer page,
        @Parameter(description = "페이지당 개수", example = "10")
        @RequestParam(required = false, defaultValue = "10") Integer limit
    );

    @ApiResponseCodes({
        CREATED,
        TEAM_RECRUITMENT_INVALID_DEADLINE_DATE,
        TEAM_RECRUITMENT_INVALID_ROLE_COMPOSITION,
        INVALID_START_DATE_AFTER_END_DATE,
        INVALID_REQUEST_BODY,
        NOT_FOUND_USER,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(summary = "모집글 작성", description = """
        ### 모집글 작성
        - `recruitment_type=ROLE_BASED`은 `roles`를 1~5개 보내며 작성자를 제외한 지원자 모집 정원은
          역할별 지원자 모집 정원의 합으로 계산됩니다.
        - 작성자를 제외한 지원자 모집 정원은 10명을 넘을 수 없습니다.
          역할별 지원자 모집 정원의 합도 같은 상한을 지켜야 합니다.
        - 역할명은 앞뒤 공백을 제거해 저장하며, 대소문자와 악센트만 다른 이름도 중복으로 봅니다.
        - `recruitment_type=GENERAL`은 작성자를 제외한 지원자 모집 정원인 `max_participants`를 보내고
          `roles`는 빈 배열로 보내셔야 합니다.
        - 지원 마감일은 활동 시작일 이하, 활동 시작일은 활동 종료일 이하여야 합니다.
        - 모집글과 TEAM 채팅방을 같은 트랜잭션에서 생성하고 작성자를 최초 채팅방 멤버로 추가합니다.
        - 별도의 팀 채팅방 생성 API는 없습니다.
        - 생성된 모집글 id만 반환합니다.
        """)
    @PostMapping
    ResponseEntity<IdResponse> createRecruitment(
        @RequestBody @Valid CreateRecruitmentRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        OK,
        TEAM_RECRUITMENT_NOT_FOUND,
    })
    @Operation(summary = "모집글 상세 조회", description = """
        ### 모집글 상세 조회
        - 비로그인 조회가 가능합니다.
        - 로그인한 경우 `is_author`, `can_apply`, `apply_block_reason`, `application`,
          `can_manage_applicants`, 팀 채팅방 정보를 함께 반환합니다.
        - `d_day`는 지원 마감일 기준이며 마감일이 지나면 null입니다.
        - 삭제된 모집글은 404를 반환합니다.
        """)
    @GetMapping("/{recruitmentId}")
    ResponseEntity<RecruitmentDetail> getRecruitment(
        @Parameter(description = "모집글 고유 식별자(recruitmentId)", example = "17") @PathVariable Integer recruitmentId,
        @UserId Integer userId
    );

    @ApiResponseCodes({
        OK,
        TEAM_RECRUITMENT_NOT_FOUND,
        TEAM_RECRUITMENT_FORBIDDEN,
        TEAM_RECRUITMENT_CLOSED,
        TEAM_RECRUITMENT_ROLE_NOT_FOUND,
        TEAM_RECRUITMENT_ROLE_UPDATE_NOT_ALLOWED,
        TEAM_RECRUITMENT_MAX_PARTICIPANTS_BELOW_ACCEPTED,
        TEAM_RECRUITMENT_TYPE_CHANGE_NOT_ALLOWED,
        TEAM_RECRUITMENT_INVALID_DEADLINE_DATE,
        TEAM_RECRUITMENT_INVALID_ROLE_COMPOSITION,
        INVALID_START_DATE_AFTER_END_DATE,
        INVALID_REQUEST_BODY,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(summary = "모집글 수정", description = """
        ### 모집글 수정
        - 작성자만 수정할 수 있습니다.
        - 기존 역할은 `id`를 반드시 보내고 새 역할은 `id`를 생략하셔야 합니다.
        - 지원자가 있는 역할은 삭제, 이름 변경, 지원자 정원 축소를 할 수 없습니다.
        - 기존 역할의 표시 순서는 유지되며, 새 역할은 기존 역할 뒤에 요청 배열 순서대로 추가됩니다.
        - `recruitment_type=GENERAL`의 `max_participants`는 작성자를 제외한 지원자 모집 정원입니다.
        - `recruitment_type=ROLE_BASED`의 지원자 모집 정원은 역할별 지원자 모집 정원의 합으로 계산됩니다.
        - 작성자를 제외한 지원자 모집 정원은 10명을 넘을 수 없습니다.
        - 역할명은 앞뒤 공백을 제거해 저장하며, 대소문자와 악센트만 다른 이름도 중복으로 봅니다.
        - 지원자 정원을 이미 승인된 인원과 같게 줄이면 그 자리에서 마감됩니다.
        - 마감된 모집글은 수정할 수 없습니다.
        """)
    @PutMapping("/{recruitmentId}")
    ResponseEntity<RecruitmentDetail> updateRecruitment(
        @Parameter(description = "모집글 고유 식별자(recruitmentId)", example = "17") @PathVariable Integer recruitmentId,
        @RequestBody @Valid UpdateRecruitmentRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        NO_CONTENT,
        TEAM_RECRUITMENT_NOT_FOUND,
        TEAM_RECRUITMENT_FORBIDDEN,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(summary = "모집글 삭제", description = """
        ### 모집글 삭제
        - 작성자만 삭제할 수 있습니다.
        - soft delete 로 처리하며 이미 삭제된 모집글에 다시 요청해도 204를 반환합니다.
        """)
    @DeleteMapping("/{recruitmentId}")
    ResponseEntity<Void> deleteRecruitment(
        @Parameter(description = "모집글 고유 식별자(recruitmentId)", example = "17") @PathVariable Integer recruitmentId,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        NO_CONTENT,
        TEAM_RECRUITMENT_NOT_FOUND,
        TEAM_RECRUITMENT_FORBIDDEN,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(summary = "모집글 마감", description = """
        ### 모집글 마감
        - 작성자만 마감할 수 있습니다.
        - 이미 마감된 모집글에 다시 요청해도 204를 반환합니다.
        - 마감 후 재개는 지원하지 않습니다.
        """)
    @PutMapping("/{recruitmentId}/close")
    ResponseEntity<Void> closeRecruitment(
        @Parameter(description = "모집글 고유 식별자(recruitmentId)", example = "17") @PathVariable Integer recruitmentId,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        OK,
        ILLEGAL_ARGUMENT,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(summary = "내가 작성한 모집글 목록", description = """
        ### 내가 작성한 모집글 목록
        - 작성자 화면용으로 지원자 수, 모집 마감 가능 여부, 팀 채팅방 정보를 포함합니다.
        - `applicant_count`는 대기 중과 승인된 지원자를 합산합니다.
        - 삭제된 모집글은 제외됩니다.
        """)
    @GetMapping("/me/created")
    ResponseEntity<CreatedRecruitmentListResponse> getMyCreatedRecruitments(
        @Parameter(description = "모집 상태 필터", example = "ALL")
        @RequestParam(required = false, defaultValue = "ALL") TeamRecruitmentStatusFilter status,
        @Parameter(description = "정렬 방식", example = "LATEST_DESC")
        @RequestParam(required = false, defaultValue = "LATEST_DESC") TeamRecruitmentSort sort,
        @Parameter(description = "페이지", example = "1")
        @RequestParam(required = false, defaultValue = "1") Integer page,
        @Parameter(description = "페이지당 개수", example = "10")
        @RequestParam(required = false, defaultValue = "10") Integer limit,
        @Auth(permit = {STUDENT}) Integer studentId
    );
}
