package in.koreatech.koin.domain.club.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.club.dto.response.ClubCategoriesResponse;
import in.koreatech.koin.domain.club.service.ClubCategoryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("club-categories")
public class ClubCategoryController implements ClubCategoryApi {

    private final ClubCategoryService clubCategoryService;

    @GetMapping
    public ResponseEntity<ClubCategoriesResponse> getClubCategories() {
        ClubCategoriesResponse response = clubCategoryService.getClubCategories();
        return ResponseEntity.ok(response);
    }
}
