package in.koreatech.koin.domain.version.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.version.dto.VersionListResponse;
import in.koreatech.koin.domain.version.service.VersionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    @PostMapping("/versions/{type}")
    public ResponseEntity<VersionListResponse> getVersions(@PathVariable(value = "type") String type)
        {
        VersionListResponse response = versionService.getVersions();
        return ResponseEntity.ok(response);
    }

}
