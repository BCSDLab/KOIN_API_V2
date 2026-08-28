package in.koreatech.koin.domain.team.recruitment.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import in.koreatech.koin.domain.team.recruitment.dto.ApplicantDetail;
import in.koreatech.koin.domain.team.recruitment.dto.ApplicantListResponse;
import in.koreatech.koin.domain.team.recruitment.dto.ApplicationCreatedResponse;
import in.koreatech.koin.domain.team.recruitment.dto.CreateApplicationRequest;
import in.koreatech.koin.domain.team.recruitment.dto.MyApplicationListResponse;
import in.koreatech.koin.domain.team.recruitment.dto.UpdateApplicationStatusRequest;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationSort;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.service.TeamRecruitmentApplicationQueryService;
import in.koreatech.koin.domain.team.recruitment.service.TeamRecruitmentApplicationService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/team-recruitments")
public class TeamRecruitmentApplicationController implements TeamRecruitmentApplicationApi {

    private final TeamRecruitmentApplicationService applicationService;
    private final TeamRecruitmentApplicationQueryService applicationQueryService;

    @PostMapping("/{recruitmentId}/applications")
    public ResponseEntity<ApplicationCreatedResponse> createApplication(
        @PathVariable("recruitmentId") Integer recruitmentId,
        @RequestBody @Valid CreateApplicationRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        ApplicationCreatedResponse response = applicationService.createApplication(request, recruitmentId, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me/applications")
    public ResponseEntity<MyApplicationListResponse> getMyApplications(
        @RequestParam(name = "statuses", required = false) List<TeamRecruitmentApplicationStatus> statuses,
        @RequestParam(name = "sort", defaultValue = "LATEST_DESC") TeamRecruitmentApplicationSort sort,
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10") Integer limit,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        MyApplicationListResponse response = applicationQueryService.getMyApplications(
            statuses, sort, page, limit, studentId
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{recruitmentId}/applications")
    public ResponseEntity<ApplicantListResponse> getApplications(
        @PathVariable("recruitmentId") Integer recruitmentId,
        @RequestParam(name = "statuses", required = false) List<TeamRecruitmentApplicationStatus> statuses,
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10") Integer limit,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        ApplicantListResponse response = applicationQueryService.getApplications(
            recruitmentId, statuses, page, limit, studentId
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{recruitmentId}/applications/{applicationId}")
    public ResponseEntity<ApplicantDetail> getApplicationDetail(
        @PathVariable("recruitmentId") Integer recruitmentId,
        @PathVariable("applicationId") Integer applicationId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        ApplicantDetail response = applicationQueryService.getApplicationDetail(
            recruitmentId, applicationId, studentId
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{recruitmentId}/applications/{applicationId}/status")
    public ResponseEntity<Void> updateApplicationStatus(
        @PathVariable("recruitmentId") Integer recruitmentId,
        @PathVariable("applicationId") Integer applicationId,
        @RequestBody @Valid UpdateApplicationStatusRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        applicationService.updateApplicationStatus(request, recruitmentId, applicationId, studentId);
        return ResponseEntity.noContent().build();
    }
}
