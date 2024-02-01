package in.koreatech.koin.domain.version.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.version.dto.VersionResponse;
import in.koreatech.koin.domain.version.service.VersionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    @GetMapping("/versions/{type}")
    public ResponseEntity<VersionResponse> getVersions(@PathVariable(value = "type") String type) {
        VersionResponse response = versionService.getVersion(type);

        return ResponseEntity.ok(response);
    }

}
