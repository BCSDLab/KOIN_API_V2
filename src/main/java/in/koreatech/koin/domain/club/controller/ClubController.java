package in.koreatech.koin.domain.club.controller;

import in.koreatech.koin.domain.club.dto.request.*;
import in.koreatech.koin.domain.club.dto.response.*;
import in.koreatech.koin.domain.club.enums.ClubEventType;
import in.koreatech.koin.domain.club.enums.ClubSortType;
import in.koreatech.koin.domain.club.service.ClubService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs")
public class ClubController implements ClubApi {

    private final ClubService clubService;

    @PostMapping
    public ResponseEntity<Void> createClubRequest(
        @RequestBody @Valid ClubCreateRequest clubCreateRequest,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubService.createClubRequest(clubCreateRequest, studentId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{clubId}")
    public ResponseEntity<ClubResponse> updateClub(
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @RequestBody @Valid ClubUpdateRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        ClubResponse response = clubService.updateClub(clubId, request, studentId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{clubId}/introduction")
    public ResponseEntity<ClubResponse> updateClubIntroduction(
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @RequestBody @Valid ClubIntroductionUpdateRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        ClubResponse response = clubService.updateClubIntroduction(clubId, request, studentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ClubsByCategoryResponse> getClubs(
        @RequestParam(required = false) Integer categoryId,
        @RequestParam(required = false, defaultValue = "false") Boolean isRecruiting,
        @RequestParam(required = false, defaultValue = "CREATED_AT_ASC") ClubSortType sortType,
        @RequestParam(required = false, defaultValue = "") String query,
        @UserId Integer userId
    ) {
        ClubsByCategoryResponse response = clubService.getClubByCategory(categoryId, isRecruiting, sortType, query, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/related")
    public ResponseEntity<ClubRelatedKeywordResponse> getRelatedClubs(
        @RequestParam(required = false, defaultValue = "") String query
    ) {
        ClubRelatedKeywordResponse response = clubService.getRelatedClubs(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{clubId}")
    public ResponseEntity<ClubResponse> getClub(
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @UserId Integer userId
    ) {
        ClubResponse response = clubService.getClub(clubId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hot")
    public ResponseEntity<ClubHotResponse> getHotClub() {
        ClubHotResponse response = clubService.getHotClub();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/empowerment")
    public ResponseEntity<Void> empowermentClubManager(
        @RequestBody @Valid ClubManagerEmpowermentRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubService.empowermentClubManager(request, studentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{clubId}/recruitment")
    public ResponseEntity<Void> createRecruitment(
        @RequestBody @Valid ClubRecruitmentCreateRequest request,
        @PathVariable(name = "clubId") Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubService.createRecruitment(request, clubId, studentId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{clubId}/recruitment")
    public ResponseEntity<Void> modifyRecruitment(
        @RequestBody @Valid ClubRecruitmentModifyRequest request,
        @PathVariable(name = "clubId") Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubService.modifyRecruitment(request, clubId, studentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{clubId}/recruitment")
    public ResponseEntity<Void> deleteRecruitment(
        @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubService.deleteRecruitment(clubId, studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{clubId}/recruitment")
    public ResponseEntity<ClubRecruitmentResponse> getRecruitment(
        @PathVariable(name = "clubId") Integer clubId,
        @UserId Integer userId
    ) {
        ClubRecruitmentResponse response = clubService.getRecruitment(clubId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("{clubId}/recruitment/notification")
    public ResponseEntity<Void> subscribeRecruitmentNotification(
        @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubService.subscribeRecruitmentNotification(clubId, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("{clubId}/recruitment/notification")
    public ResponseEntity<Void> rejectRecruitmentNotification(
        @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubService.rejectRecruitmentNotification(clubId, studentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{clubId}/event")
    public ResponseEntity<Void> createClubEvent(
        @PathVariable Integer clubId,
        @RequestBody @Valid ClubEventCreateRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubService.createClubEvent(request, clubId, studentId);
        return ResponseEntity.ok().build();
    };

    @PutMapping("/{clubId}/event/{eventId}")
    public ResponseEntity<Void> modifyClubEvent(
        @PathVariable Integer clubId,
        @PathVariable Integer eventId,
        @RequestBody @Valid ClubEventModifyRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubService.modifyClubEvent(request, eventId, clubId, studentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{clubId}/event/{eventId}")
    public ResponseEntity<Void> deleteClubEvent(
        @PathVariable Integer clubId,
        @PathVariable Integer eventId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubService.deleteClubEvent(clubId, eventId, studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{clubId}/event/{eventId}")
    public ResponseEntity<ClubEventResponse> getClubEvent(
        @PathVariable Integer clubId,
        @PathVariable Integer eventId
    ) {
        ClubEventResponse response = clubService.getClubEvent(clubId, eventId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{clubId}/events")
    public ResponseEntity<List<ClubEventsResponse>> getClubEvents(
        @PathVariable Integer clubId,
        @RequestParam(defaultValue = "RECENT") ClubEventType eventType,
        @UserId Integer userId
    ) {
        List<ClubEventsResponse> responses = clubService.getClubEvents(clubId, eventType, userId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("{clubId}/event/{eventId}/notification")
    public ResponseEntity<Void> subscribeEventNotification(
        @PathVariable Integer clubId,
        @PathVariable Integer eventId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubService.subscribeEventNotification(clubId, eventId, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("{clubId}/event/{eventId}/notification")
    public ResponseEntity<Void> rejectEventNotification(
        @PathVariable Integer clubId,
        @PathVariable Integer eventId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubService.rejectEventNotification(clubId, eventId, studentId);
        return ResponseEntity.noContent().build();
    }
}
