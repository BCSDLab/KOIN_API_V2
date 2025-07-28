package in.koreatech.koin.domain.community.article.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.community.article.dto.LostItemReportRequest;
import in.koreatech.koin.domain.community.article.service.LostItemReportService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LostItemReportController implements LostItemReportApi{

    private final LostItemReportService lostItemReportService;

    @PostMapping("/articles/lost-item/{id}/reports")
    public ResponseEntity<Void> reportLostItemArticle(
        @PathVariable Integer id,
        @RequestBody @Valid LostItemReportRequest lostItemReportRequest,
        @Auth(permit = {STUDENT, COUNCIL}) Integer studentId
    ) {
        lostItemReportService.reportLostItemArticle(id, studentId, lostItemReportRequest);
        return ResponseEntity.noContent().build();
    }
}
