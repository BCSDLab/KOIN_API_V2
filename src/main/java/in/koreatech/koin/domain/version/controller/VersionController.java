package in.koreatech.koin.domain.version.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.version.dto.VersionMessageResponse;
import in.koreatech.koin.domain.version.dto.VersionResponse;
import in.koreatech.koin.domain.version.service.VersionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class VersionController implements VersionApi {

    private final VersionService versionService;

    // 백엔드 내부 사용 메서드
    public ResponseEntity<VersionResponse> getVersion(String type) {
        VersionResponse response = versionService.getVersion(type);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/version/{type}")
    public ResponseEntity<VersionMessageResponse> getVersionWithMessage(@PathVariable(value = "type") String type) {
        VersionMessageResponse response = versionService.getVersionWithMessage(type);

        return ResponseEntity.ok(response);
    }
}
