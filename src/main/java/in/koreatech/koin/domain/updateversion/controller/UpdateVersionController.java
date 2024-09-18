package in.koreatech.koin.domain.updateversion.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.updateversion.dto.UpdateVersionResponse;
import in.koreatech.koin.domain.updateversion.model.UpdateVersionType;
import in.koreatech.koin.domain.updateversion.service.UpdateVersionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UpdateVersionController implements UpdateVersionApi {

    private final UpdateVersionService updateVersionService;

    @GetMapping("/update/version/{type}")
    public ResponseEntity<UpdateVersionResponse> getVersion(@PathVariable(value = "type") UpdateVersionType type) {
        UpdateVersionResponse response = updateVersionService.getVersion(type);

        return ResponseEntity.ok(response);
    }
}
