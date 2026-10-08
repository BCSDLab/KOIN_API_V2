package in.koreatech.koin.domain.dining.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.dining.dto.DiningReportCreateRequest;
import in.koreatech.koin.domain.dining.dto.DiningReportCreateResponse;
import in.koreatech.koin.domain.dining.service.DiningReportService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DiningReportController implements DiningReportApi {

    private final DiningReportService diningReportService;

    @PostMapping("/dinings/{diningId}/soldout-reports")
    public ResponseEntity<DiningReportCreateResponse> createReport(
        @Auth(permit = STUDENT) Integer userId,
        @PathVariable Integer diningId,
        @Valid @RequestBody DiningReportCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(diningReportService.create(userId, diningId, request));
    }
}
