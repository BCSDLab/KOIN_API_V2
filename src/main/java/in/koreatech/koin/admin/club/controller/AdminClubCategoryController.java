package in.koreatech.koin.admin.club.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.admin.club.dto.response.AdminClubCategoriesResponse;
import in.koreatech.koin.admin.club.service.AdminClubCategoryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/club-categories")
public class AdminClubCategoryController implements AdminClubCategoryApi {

    private final AdminClubCategoryService adminClubCategoryService;

    @GetMapping
    public ResponseEntity<AdminClubCategoriesResponse> getClubCategories(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminClubCategoriesResponse response = adminClubCategoryService.getClubCategories();
        return ResponseEntity.ok(response);
    }
}
