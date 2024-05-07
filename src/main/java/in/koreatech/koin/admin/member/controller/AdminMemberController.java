package in.koreatech.koin.admin.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.member.dto.AdminMembersResponse;
import in.koreatech.koin.admin.member.enums.TrackTag;
import in.koreatech.koin.admin.member.service.AdminMemberService;
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
        @RequestParam(name = "is_deleted", defaultValue = "false") Boolean isDeleted
    ) {
        return ResponseEntity.ok().body(adminMemberService.getMembers(page, limit, track, isDeleted));
    }
}