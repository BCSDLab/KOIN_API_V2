package in.koreatech.koin.domain.club.club.controller;

import in.koreatech.koin.domain.club.club.dto.request.ClubCreateRequest;
import in.koreatech.koin.domain.club.club.dto.request.ClubIntroductionUpdateRequest;
import in.koreatech.koin.domain.club.club.dto.request.ClubUpdateRequest;
import in.koreatech.koin.domain.club.club.dto.response.ClubHotResponse;
import in.koreatech.koin.domain.club.club.dto.response.ClubRelatedKeywordResponse;
import in.koreatech.koin.domain.club.club.dto.response.ClubResponse;
import in.koreatech.koin.domain.club.club.dto.response.ClubsByCategoryResponse;
import in.koreatech.koin.domain.club.club.enums.ClubSortType;
import in.koreatech.koin.domain.club.club.service.ClubService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
