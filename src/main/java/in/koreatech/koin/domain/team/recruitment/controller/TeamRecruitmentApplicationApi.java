package in.koreatech.koin.domain.team.recruitment.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static in.koreatech.koin.global.code.ApiResponseCode.CREATED;
import static in.koreatech.koin.global.code.ApiResponseCode.ILLEGAL_ARGUMENT;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.NO_CONTENT;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_READABLE_HTTP_MESSAGE;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;
import static in.koreatech.koin.global.code.ApiResponseCode.OPTIMISTIC_LOCKING_FAILURE;
import static in.koreatech.koin.global.code.ApiResponseCode.REQUEST_TOO_FAST;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_APPLICATION_DUPLICATE;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_APPLICATION_FINALIZED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_APPLICATION_NOT_FOUND;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CAPACITY_FULL;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CLOSED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_FORBIDDEN;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_NOT_FOUND;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_PROFILE_REQUIRED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_ROLE_CLOSED;
import static in.koreatech.koin.global.code.ApiResponseCode.UNAUTHORIZED_USER;

import java.util.List;

import in.koreatech.koin.domain.team.recruitment.dto.ApplicantDetail;
import in.koreatech.koin.domain.team.recruitment.dto.ApplicantListResponse;
import in.koreatech.koin.domain.team.recruitment.dto.ApplicationCreatedResponse;
import in.koreatech.koin.domain.team.recruitment.dto.CreateApplicationRequest;
import in.koreatech.koin.domain.team.recruitment.dto.MyApplicationListResponse;
import in.koreatech.koin.domain.team.recruitment.dto.UpdateApplicationStatusRequest;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationSort;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "(Normal) Team Recruitment: 팀원 모집", description = "팀원 모집 지원서와 지원자를 관리한다")
@RequestMapping("/team-recruitments")
public interface TeamRecruitmentApplicationApi {

    @ApiResponseCodes({
        CREATED,
        INVALID_REQUEST_BODY,
        NOT_READABLE_HTTP_MESSAGE,
        UNAUTHORIZED_USER,
        TEAM_RECRUITMENT_FORBIDDEN,
        TEAM_RECRUITMENT_NOT_FOUND,
        TEAM_RECRUITMENT_PROFILE_REQUIRED,
        TEAM_RECRUITMENT_CLOSED,
        TEAM_RECRUITMENT_ROLE_CLOSED,
        TEAM_RECRUITMENT_CAPACITY_FULL,
        TEAM_RECRUITMENT_APPLICATION_DUPLICATE,
        REQUEST_TOO_FAST
    })
    @Operation(summary = "지원서 작성")
    @PostMapping("/{recruitmentId}/applications")
    ResponseEntity<ApplicationCreatedResponse> createApplication(
        @Parameter(description = "모집글 ID", example = "17", required = true)
        @PathVariable("recruitmentId") Integer recruitmentId,
        @RequestBody @Valid CreateApplicationRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        OK,
        ILLEGAL_ARGUMENT,
        UNAUTHORIZED_USER,
        TEAM_RECRUITMENT_FORBIDDEN
    })
    @Operation(summary = "내가 지원한 모집 목록")
    @GetMapping("/me/applications")
    ResponseEntity<MyApplicationListResponse> getMyApplications(
        @RequestParam(name = "statuses", required = false) List<TeamRecruitmentApplicationStatus> statuses,
        @RequestParam(name = "sort", defaultValue = "LATEST_DESC") TeamRecruitmentApplicationSort sort,
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10") Integer limit,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        OK,
        ILLEGAL_ARGUMENT,
        UNAUTHORIZED_USER,
        TEAM_RECRUITMENT_FORBIDDEN,
        TEAM_RECRUITMENT_NOT_FOUND
    })
    @Operation(summary = "지원자 목록 조회")
    @GetMapping("/{recruitmentId}/applications")
    ResponseEntity<ApplicantListResponse> getApplications(
        @Parameter(description = "모집글 ID", example = "17", required = true)
        @PathVariable("recruitmentId") Integer recruitmentId,
        @RequestParam(name = "statuses", required = false) List<TeamRecruitmentApplicationStatus> statuses,
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10") Integer limit,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        OK,
        ILLEGAL_ARGUMENT,
        UNAUTHORIZED_USER,
        TEAM_RECRUITMENT_FORBIDDEN,
        TEAM_RECRUITMENT_NOT_FOUND,
        TEAM_RECRUITMENT_APPLICATION_NOT_FOUND
    })
    @Operation(summary = "지원자 상세 조회")
    @GetMapping("/{recruitmentId}/applications/{applicationId}")
    ResponseEntity<ApplicantDetail> getApplicationDetail(
        @Parameter(description = "모집글 ID", example = "17", required = true)
        @PathVariable("recruitmentId") Integer recruitmentId,
        @Parameter(description = "지원서 ID", example = "51", required = true)
        @PathVariable("applicationId") Integer applicationId,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        NO_CONTENT,
        INVALID_REQUEST_BODY,
        UNAUTHORIZED_USER,
        TEAM_RECRUITMENT_FORBIDDEN,
        TEAM_RECRUITMENT_NOT_FOUND,
        TEAM_RECRUITMENT_APPLICATION_NOT_FOUND,
        TEAM_RECRUITMENT_CLOSED,
        TEAM_RECRUITMENT_ROLE_CLOSED,
        TEAM_RECRUITMENT_CAPACITY_FULL,
        TEAM_RECRUITMENT_APPLICATION_FINALIZED,
        OPTIMISTIC_LOCKING_FAILURE
    })
    @Operation(summary = "지원 승인 또는 거절")
    @PutMapping("/{recruitmentId}/applications/{applicationId}/status")
    ResponseEntity<Void> updateApplicationStatus(
        @Parameter(description = "모집글 ID", example = "17", required = true)
        @PathVariable("recruitmentId") Integer recruitmentId,
        @Parameter(description = "지원서 ID", example = "51", required = true)
        @PathVariable("applicationId") Integer applicationId,
        @RequestBody @Valid UpdateApplicationStatusRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    );
}
