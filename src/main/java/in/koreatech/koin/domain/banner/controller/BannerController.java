package in.koreatech.koin.domain.banner.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.domain.banner.dto.ChangeBannerActiveRequest;
import in.koreatech.koin.domain.banner.dto.ChangeBannerPriorityRequest;
import in.koreatech.koin.domain.banner.dto.ModifyBannerRequest;
import in.koreatech.koin.domain.banner.service.BannerService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/banners")
public class BannerController {

    private final BannerService bannerService;

    @PatchMapping("/{id}/priority")
    public ResponseEntity<Void> changePriority(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody @Valid ChangeBannerPriorityRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        bannerService.changePriority(id, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> changeActive(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody @Valid ChangeBannerActiveRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        bannerService.changeActive(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> modifyBanner(
        @Parameter(in = PATH) @PathVariable Integer id,
        @RequestBody @Valid ModifyBannerRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        // bannerService.modifyBanner(id, request);
        return ResponseEntity.ok().build();
    }
}
