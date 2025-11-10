package in.koreatech.koin.admin.keyword.controller;

import static in.koreatech.koin.admin.history.enums.DomainType.KEYWORDS;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.history.aop.AdminActivityLogging;
import in.koreatech.koin.admin.keyword.dto.AdminFilteredKeywordsResponse;
import in.koreatech.koin.admin.keyword.dto.AdminKeywordFilterRequest;
import in.koreatech.koin.admin.keyword.service.AdminKeywordService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/articles/keyword")
public class AdminKeywordController implements AdminKeywordApi {

    private final AdminKeywordService adminKeywordService;

    @PostMapping("/filter")
    @AdminActivityLogging(domain = KEYWORDS)
    public ResponseEntity<Void> toggleKeywordFilter(
        @Valid @RequestBody AdminKeywordFilterRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminKeywordService.filterKeyword(request.keyword(), request.isFiltered());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/filtered")
    public ResponseEntity<AdminFilteredKeywordsResponse> getFilteredKeywords(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminFilteredKeywordsResponse response = adminKeywordService.getFilteredKeywords();
        return ResponseEntity.ok(response);
    }
}
