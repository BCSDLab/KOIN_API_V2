package in.koreatech.koin.admin.land.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.land.dto.AdminLandResponse;
import in.koreatech.koin.admin.land.dto.AdminLandsRequest;
import in.koreatech.koin.admin.land.dto.AdminLandsResponse;
import in.koreatech.koin.admin.land.service.AdminLandService;
import in.koreatech.koin.admin.member.dto.AdminMemberResponse;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminLandController implements AdminLandApi {

    private final AdminLandService adminLandService;

    @GetMapping("/admin/lands")
    public ResponseEntity<AdminLandsResponse> getLands(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @RequestParam(name = "is_deleted", defaultValue = "false") Boolean isDeleted,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminLandService.getLands(page, limit, isDeleted));
    }

    @PostMapping("/admin/lands")
    public ResponseEntity<AdminLandsResponse> postLands(
        @RequestBody @Valid AdminLandsRequest adminLandsRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminLandService.createLands(adminLandsRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/admin/lands/{id}")
    public ResponseEntity<Void> deleteLand(
        @PathVariable("id") Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminLandService.deleteLand(id);
        return null;
    }

    @GetMapping("/admin/lands/{id}")
    public ResponseEntity<AdminLandResponse> getLand(
        @PathVariable("id") Integer id
        //@Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminLandService.getLand(id));
    }

}
