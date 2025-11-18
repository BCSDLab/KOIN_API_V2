package in.koreatech.koin.admin.club.controller;

import static in.koreatech.koin.admin.history.enums.DomainType.CLUBS;
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

import in.koreatech.koin.admin.club.dto.request.AdminClubActiveChangeRequest;
import in.koreatech.koin.admin.club.dto.request.AdminClubCreateRequest;
import in.koreatech.koin.admin.club.dto.request.AdminClubManagerCondition;
import in.koreatech.koin.admin.club.dto.request.AdminClubManagerDecideRequest;
import in.koreatech.koin.admin.club.dto.request.AdminClubModifyRequest;
import in.koreatech.koin.admin.club.dto.request.AdminPendingClubRequest;
import in.koreatech.koin.admin.club.dto.response.AdminClubManagersResponse;
import in.koreatech.koin.admin.club.dto.response.AdminClubResponse;
import in.koreatech.koin.admin.club.dto.response.AdminClubsResponse;
import in.koreatech.koin.admin.club.dto.response.AdminPendingClubResponse;
import in.koreatech.koin.admin.club.service.AdminClubService;
import in.koreatech.koin.admin.history.aop.AdminActivityLogging;
import in.koreatech.koin.global.auth.Auth;
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
    @AdminActivityLogging(domain = CLUBS)
    public ResponseEntity<Void> createClub(
        @RequestBody @Valid AdminClubCreateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminClubService.createClub(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{clubId}")
    @AdminActivityLogging(domain = CLUBS, domainIdParam = "clubId")
    public ResponseEntity<Void> modifyClub(
        @Parameter(in = PATH) @PathVariable(name = "clubId") Integer clubId,
        @RequestBody @Valid AdminClubModifyRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminClubService.modifyClub(clubId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{clubId}/active")
    @AdminActivityLogging(domain = CLUBS, domainIdParam = "clubId")
    public ResponseEntity<Void> changeActive(
        @PathVariable Integer clubId,
        @RequestBody @Valid AdminClubActiveChangeRequest request,
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

    @PostMapping("/pending")
    @AdminActivityLogging(domain = CLUBS)
    public ResponseEntity<AdminPendingClubResponse> getPendingClub(
        @RequestBody @Valid AdminPendingClubRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminPendingClubResponse response = adminClubService.getPendingClub(request.clubName());
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<AdminClubManagersResponse> getPendingClubManagers(
        @ParameterObject @ModelAttribute AdminClubManagerCondition AdminClubManagerCondition,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminClubService.getPendingClubManagers(AdminClubManagerCondition));
    }

    @PostMapping("/decision")
    @AdminActivityLogging(domain = CLUBS)
    public ResponseEntity<Void> decideClubAdmin(
        @RequestBody @Valid AdminClubManagerDecideRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminClubService.decideClubManager(request.clubName(), request);
        return ResponseEntity.ok().build();
    }
}
