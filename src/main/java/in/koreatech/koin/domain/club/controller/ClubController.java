package in.koreatech.koin.domain.club.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

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

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin._common.auth.UserId;
import in.koreatech.koin.domain.club.dto.request.ClubCreateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubIntroductionUpdateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubManagerEmpowermentRequest;
import in.koreatech.koin.domain.club.dto.request.ClubRecruitmentCreateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubRecruitmentModifyRequest;
import in.koreatech.koin.domain.club.dto.request.ClubUpdateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubQnaCreateRequest;
import in.koreatech.koin.domain.club.dto.response.ClubHotResponse;
import in.koreatech.koin.domain.club.dto.response.ClubRelatedKeywordResponse;
import in.koreatech.koin.domain.club.dto.response.ClubResponse;
import in.koreatech.koin.domain.club.dto.response.ClubsByCategoryResponse;
import in.koreatech.koin.domain.club.dto.response.ClubQnasResponse;
import in.koreatech.koin.domain.club.enums.ClubSortType;
import in.koreatech.koin.domain.club.service.ClubService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<ClubsByCategoryResponse> getClubByCategory(
        @RequestParam(required = false) Integer categoryId,
        @RequestParam(required = false, defaultValue = "false") Boolean isRecruiting,
        @RequestParam(required = false, defaultValue = "NONE") ClubSortType sortType,
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

    @PutMapping("/{clubId}/like")
    public ResponseEntity<Void> likeClub(
        @Auth(permit = {STUDENT}) Integer userId,
        @Parameter(in = PATH) @PathVariable Integer clubId
    ) {
        clubService.likeClub(clubId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{clubId}/like/cancel")
    public ResponseEntity<Void> likeClubCancel(
        @Auth(permit = {STUDENT}) Integer userId,
        @Parameter(in = PATH) @PathVariable Integer clubId
    ) {
        clubService.likeClubCancel(clubId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/hot")
    public ResponseEntity<ClubHotResponse> getHotClub() {
        ClubHotResponse response = clubService.getHotClub();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{clubId}/qna")
    public ResponseEntity<ClubQnasResponse> getQnas(
        @Parameter(in = PATH) @PathVariable Integer clubId
    ) {
        ClubQnasResponse response = clubService.getQnas(clubId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{clubId}/qna")
    public ResponseEntity<Void> createQna(
        @RequestBody @Valid ClubQnaCreateRequest request,
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubService.createQna(request, clubId, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{clubId}/qna/{qnaId}")
    public ResponseEntity<Void> deleteQna(
        @Parameter(in = PATH) @PathVariable Integer clubId,
        @Parameter(in = PATH) @PathVariable Integer qnaId,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubService.deleteQna(clubId, qnaId, studentId);
        return ResponseEntity.noContent().build();
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
