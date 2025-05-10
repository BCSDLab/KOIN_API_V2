package in.koreatech.koin.admin.club.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.admin.club.dto.response.AdminClubsResponse;
import in.koreatech.koin.admin.club.service.AdminClubService;
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
        @RequestParam(name = "sort_by_like", required = false) Boolean sortByLike,
        @RequestParam(name = "club_category_name", required = false, defaultValue = "전체") String clubCategoryName,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminClubsResponse response = adminClubService.getClubs(page, limit, sortByLike, clubCategoryName);
        return ResponseEntity.ok(response);
    }
}
