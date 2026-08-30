package in.koreatech.koin.domain.team.recruitment.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import in.koreatech.koin.domain.team.recruitment.service.TeamRecruitmentQueryService;
import in.koreatech.koin.domain.team.recruitment.service.TeamRecruitmentService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/team-recruitments")
public class TeamRecruitmentController implements TeamRecruitmentApi {

    private final TeamRecruitmentService teamRecruitmentService;
    private final TeamRecruitmentQueryService teamRecruitmentQueryService;

    @GetMapping
    public ResponseEntity<RecruitmentListResponse> getRecruitments(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false, defaultValue = "ALL") TeamRecruitmentStatusFilter status,
        @RequestParam(required = false) List<TeamRecruitmentCategory> categories,
        @RequestParam(required = false) TeamRecruitmentMeetingType meetingType,
        @RequestParam(required = false, defaultValue = "LATEST_DESC") TeamRecruitmentSort sort,
        @RequestParam(required = false, defaultValue = "1") Integer page,
        @RequestParam(required = false, defaultValue = "10") Integer limit
    ) {
        RecruitmentListResponse response = teamRecruitmentQueryService.getRecruitments(
            keyword, status, categories, meetingType, sort, page, limit);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<IdResponse> createRecruitment(
        @RequestBody @Valid CreateRecruitmentRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        IdResponse response = teamRecruitmentService.createRecruitment(studentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{recruitmentId}")
    public ResponseEntity<RecruitmentDetail> getRecruitment(
        @PathVariable Integer recruitmentId,
        @UserId Integer userId
    ) {
        RecruitmentDetail response = teamRecruitmentQueryService.getRecruitment(recruitmentId, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{recruitmentId}")
    public ResponseEntity<RecruitmentDetail> updateRecruitment(
        @PathVariable Integer recruitmentId,
        @RequestBody @Valid UpdateRecruitmentRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        teamRecruitmentService.updateRecruitment(studentId, recruitmentId, request);
        RecruitmentDetail response = teamRecruitmentQueryService.getRecruitment(recruitmentId, studentId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{recruitmentId}")
    public ResponseEntity<Void> deleteRecruitment(
        @PathVariable Integer recruitmentId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        teamRecruitmentService.deleteRecruitment(studentId, recruitmentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{recruitmentId}/close")
    public ResponseEntity<Void> closeRecruitment(
        @PathVariable Integer recruitmentId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        teamRecruitmentService.closeRecruitment(studentId, recruitmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/created")
    public ResponseEntity<CreatedRecruitmentListResponse> getMyCreatedRecruitments(
        @RequestParam(required = false, defaultValue = "ALL") TeamRecruitmentStatusFilter status,
        @RequestParam(required = false, defaultValue = "LATEST_DESC") TeamRecruitmentSort sort,
        @RequestParam(required = false, defaultValue = "1") Integer page,
        @RequestParam(required = false, defaultValue = "10") Integer limit,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        CreatedRecruitmentListResponse response = teamRecruitmentQueryService.getMyCreatedRecruitments(
            studentId, status, sort, page, limit);
        return ResponseEntity.ok(response);
    }
}
