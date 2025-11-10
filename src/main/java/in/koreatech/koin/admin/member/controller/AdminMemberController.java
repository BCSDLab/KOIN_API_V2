package in.koreatech.koin.admin.member.controller;

import static in.koreatech.koin.admin.history.enums.DomainType.MEMBERS;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.history.aop.AdminActivityLogging;
import in.koreatech.koin.admin.member.dto.AdminMemberRequest;
import in.koreatech.koin.admin.member.dto.AdminMemberResponse;
import in.koreatech.koin.admin.member.dto.AdminMembersResponse;
import in.koreatech.koin.admin.member.enums.TrackTag;
import in.koreatech.koin.admin.member.service.AdminMemberService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminMemberController implements AdminMemberApi {

    private final AdminMemberService adminMemberService;

    @GetMapping("/admin/members")
    public ResponseEntity<AdminMembersResponse> getMembers(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "50", required = false) Integer limit,
        @RequestParam(name = "track") TrackTag track,
        @RequestParam(name = "is_deleted", defaultValue = "false") Boolean isDeleted,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminMemberService.getMembers(page, limit, track, isDeleted));
    }

    @PostMapping("/admin/members")
    @AdminActivityLogging(domain = MEMBERS)
    public ResponseEntity<Void> createMember(
        @RequestBody @Valid AdminMemberRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminMemberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/admin/members/{id}")
    public ResponseEntity<AdminMemberResponse> getMember(
        @PathVariable("id") Integer memberId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminMemberService.getMember(memberId));
    }

    @DeleteMapping("/admin/members/{id}")
    @AdminActivityLogging(domain = MEMBERS, domainIdParam = "memberId")
    public ResponseEntity<Void> deleteMember(
        @PathVariable("id") Integer memberId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminMemberService.deleteMember(memberId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/members/{id}")
    @AdminActivityLogging(domain = MEMBERS, domainIdParam = "memberId")
    public ResponseEntity<Void> updateMember(
        @PathVariable("id") Integer memberId,
        @RequestBody @Valid AdminMemberRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminMemberService.updateMember(memberId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/members/{id}/undelete")
    @AdminActivityLogging(domain = MEMBERS, domainIdParam = "memberId")
    public ResponseEntity<Void> undeleteMember(
        @PathVariable("id") Integer memberId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminMemberService.undeleteMember(memberId);
        return ResponseEntity.ok().build();
    }
}
