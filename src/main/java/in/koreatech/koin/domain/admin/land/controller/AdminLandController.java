package in.koreatech.koin.domain.admin.land.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.admin.land.dto.AdminLandsResponse;
import in.koreatech.koin.domain.admin.land.service.AdminLandService;
import in.koreatech.koin.domain.land.dto.LandsGroupResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminLandController implements AdminLandApi {

    private final AdminLandService adminLandService;

    @Override
    @GetMapping("/admin/lands")
    public ResponseEntity<AdminLandsResponse> getLands(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @RequestParam(name = "is_deleted", defaultValue = "false") Boolean isDeleted
    ) {
        return ResponseEntity.ok().body(adminLandService.getLands(page, limit, isDeleted));
    }
}