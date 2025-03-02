package in.koreatech.koin.admin.lostItem.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.lostItem.dto.AdminLostItemArticlesResponse;
import in.koreatech.koin.admin.lostItem.dto.AdminModifyLostItemArticleReportStatusRequest;
import in.koreatech.koin.admin.lostItem.service.AdminLostItemArticleService;
import in.koreatech.koin._common.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminLostItemArticleController implements AdminLostItemArticleApi{

    private final AdminLostItemArticleService adminLostItemArticleService;

    @GetMapping("/admin/articles/lost-item")
    public ResponseEntity<AdminLostItemArticlesResponse> getLostItemArticles(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(adminLostItemArticleService.getAllLostItemArticles(page, limit));
    }

    @PutMapping("/admin/articles/lost-item/reports/{id}")
    public ResponseEntity<Void> modifyLostItemReportStatus(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody @Valid AdminModifyLostItemArticleReportStatusRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminLostItemArticleService.modifyLostItemArticleReportStatus(id, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/admin/articles/lost-item/{id}")
    public ResponseEntity<Void> deleteLostItemArticle(
        @Parameter(in = PATH) @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminLostItemArticleService.deleteLostItemArticle(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
