package in.koreatech.koin.domain.updateversion.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.updateversion.dto.UpdateVersionResponse;
import in.koreatech.koin.domain.updateversion.model.UpdateVersion;
import in.koreatech.koin.domain.updateversion.model.UpdateVersionType;
import in.koreatech.koin.domain.updateversion.repository.UpdateVersionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UpdateVersionService {

    private final UpdateVersionRepository updateVersionRepository;

    public UpdateVersionResponse getVersion(UpdateVersionType type) {
        UpdateVersion version = updateVersionRepository.getByType(type);
        return UpdateVersionResponse.from(version);
    }
}
