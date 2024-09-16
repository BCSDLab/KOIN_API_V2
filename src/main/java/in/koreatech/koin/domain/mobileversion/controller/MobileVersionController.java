package in.koreatech.koin.domain.mobileversion.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.mobileversion.dto.MobileVersionResponse;
import in.koreatech.koin.domain.mobileversion.service.MobileVersionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MobileVersionController implements MobileVersionApi {

    private final MobileVersionService mobileVersionService;

    @GetMapping("/versions/{type}")
    public ResponseEntity<MobileVersionResponse> getVersions(@PathVariable(value = "type") String type) {
        MobileVersionResponse response = mobileVersionService.getVersion(type);

        return ResponseEntity.ok(response);
    }
}
