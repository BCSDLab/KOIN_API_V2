package in.koreatech.koin.admin.club.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.admin.club.dto.request.AdminClubManagerCondition;
import in.koreatech.koin.admin.club.dto.request.AdminChangeClubActiveRequest;
import in.koreatech.koin.admin.club.dto.request.AdminCreateClubRequest;
import in.koreatech.koin.admin.club.dto.request.AdminDecideClubManagerRequest;
import in.koreatech.koin.admin.club.dto.request.AdminModifyClubRequest;
import in.koreatech.koin.admin.club.dto.response.AdminClubManagersResponse;
import in.koreatech.koin.admin.club.dto.response.AdminClubResponse;
import in.koreatech.koin.admin.club.dto.response.AdminClubsResponse;
import in.koreatech.koin.admin.club.dto.response.AdminNewClubResponse;
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
        @RequestParam(name = "club_category_id", required = false) Integer clubCategoryId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminClubsResponse response = adminClubService.getClubs(page, limit, clubCategoryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{clubId}")
    public ResponseEntity<AdminClubResponse> getClub(
        @PathVariable(value = "clubId") Integer clubId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminClubResponse response = adminClubService.getClub(clubId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> createClub(
        @RequestBody @Valid AdminCreateClubRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminClubService.createClub(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{clubId}")
    public ResponseEntity<Void> modifyClub(
        @Parameter(in = PATH) @PathVariable(name = "clubId") Integer clubId,
        @RequestBody @Valid AdminModifyClubRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminClubService.modifyClub(clubId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{clubId}/active")
    public ResponseEntity<Void> changeActive(
        @PathVariable Integer clubId,
        @RequestBody @Valid AdminChangeClubActiveRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminClubService.changeActive(clubId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/managers")
    public ResponseEntity<AdminClubManagersResponse> getClubManagers(
        @ParameterObject @ModelAttribute AdminClubManagerCondition AdminClubManagerCondition,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminClubService.getClubAdmins(AdminClubManagerCondition));
    }

    @GetMapping("/new-club")
    public ResponseEntity<AdminNewClubResponse> getNewClub(
        @RequestParam String clubName,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminNewClubResponse response = adminClubService.getNewClub(clubName);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/new-clubs")
    public ResponseEntity<AdminClubManagersResponse> getNewClubManagers(
        @ParameterObject @ModelAttribute AdminClubManagerCondition AdminClubManagerCondition,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminClubService.getUnacceptedClubManagers(AdminClubManagerCondition));
    }

    @PostMapping("/decision")
    public ResponseEntity<Void> decideClubAdmin(
        @RequestBody AdminDecideClubManagerRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminClubService.decideClubAdmin(request.clubName(), request);
        return ResponseEntity.ok().build();
    }
}
