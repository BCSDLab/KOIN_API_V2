package in.koreatech.koin.admin.club.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.admin.club.dto.request.CreateAdminClubRequest;
import in.koreatech.koin.admin.club.dto.request.ModifyAdminClubRequest;
import in.koreatech.koin.admin.club.dto.response.AdminClubResponse;
import in.koreatech.koin.admin.club.dto.response.AdminClubsResponse;
import in.koreatech.koin.admin.club.service.AdminClubService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/clubs")
public class AdminClubController implements AdminClubApi {

    private final AdminClubService adminClubService;

    @GetMapping
    public ResponseEntity<AdminClubsResponse> getClubs(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @RequestParam(name = "sort_by_like", defaultValue = "false", required = false) Boolean sortByLike,
        @RequestParam(name = "club_category_id", required = false) Integer clubCategoryId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminClubsResponse response = adminClubService.getClubs(page, limit, sortByLike, clubCategoryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cludId}")
    public ResponseEntity<AdminClubResponse> getClub(
        @PathVariable(value = "cludId") Integer clubId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminClubResponse response = adminClubService.getClub(clubId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> createClub(
        @RequestBody @Valid CreateAdminClubRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminClubService.createClub(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{cludId}")
    public ResponseEntity<Void> modifyClub(
        @Parameter(in = PATH) @PathVariable(name = "cludId") Integer clubId,
        @RequestBody @Valid ModifyAdminClubRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminClubService.modifyClub(clubId, request);
        return ResponseEntity.ok().build();
    }
}
