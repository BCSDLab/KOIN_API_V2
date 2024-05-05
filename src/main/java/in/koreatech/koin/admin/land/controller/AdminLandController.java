package in.koreatech.koin.admin.land.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.land.dto.AdminLandsRequest;
import in.koreatech.koin.admin.land.dto.AdminLandsResponse;
import in.koreatech.koin.admin.land.service.AdminLandService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
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

    @Override
    @PostMapping("/admin/lands")
    public ResponseEntity<AdminLandsResponse> postLands(@RequestBody @Valid AdminLandsRequest adminLandsRequest) {
        adminLandService.createLands(adminLandsRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
