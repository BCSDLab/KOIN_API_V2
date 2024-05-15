package in.koreatech.koin.admin.member.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.member.dto.AdminMemberRequest;
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
    public ResponseEntity<Void> createMember(@RequestBody @Valid AdminMemberRequest request) {
        adminMemberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
