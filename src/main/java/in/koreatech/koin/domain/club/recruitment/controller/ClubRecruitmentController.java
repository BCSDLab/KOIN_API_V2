package in.koreatech.koin.domain.club.recruitment.controller;

import in.koreatech.koin.domain.club.recruitment.dto.request.ClubRecruitmentCreateRequest;
import in.koreatech.koin.domain.club.recruitment.dto.request.ClubRecruitmentModifyRequest;
import in.koreatech.koin.domain.club.recruitment.dto.response.ClubRecruitmentResponse;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs/{clubId}/recruitment")
public class ClubRecruitmentController {

    private final ClubRecruitmentService clubRecruitmentService;

    @PostMapping
    public ResponseEntity<Void> createRecruitment(
        @RequestBody @Valid ClubRecruitmentCreateRequest request,
        @PathVariable(name = "clubId") Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubRecruitmentService.createRecruitment(request, clubId, studentId);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> modifyRecruitment(
        @RequestBody @Valid ClubRecruitmentModifyRequest request,
        @PathVariable(name = "clubId") Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubRecruitmentService.modifyRecruitment(request, clubId, studentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteRecruitment(
        @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubRecruitmentService.deleteRecruitment(clubId, studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ClubRecruitmentResponse> getRecruitment(
        @PathVariable(name = "clubId") Integer clubId,
        @UserId Integer userId
    ) {
        ClubRecruitmentResponse response = clubRecruitmentService.getRecruitment(clubId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/notification")
    public ResponseEntity<Void> subscribeRecruitmentNotification(
        @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubRecruitmentService.subscribeRecruitmentNotification(clubId, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/notification")
    public ResponseEntity<Void> rejectRecruitmentNotification(
        @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubRecruitmentService.rejectRecruitmentNotification(clubId, studentId);
        return ResponseEntity.noContent().build();
    }
}
