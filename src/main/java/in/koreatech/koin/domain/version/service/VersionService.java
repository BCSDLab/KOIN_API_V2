package in.koreatech.koin.domain.version.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.version.dto.VersionResponse;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VersionService {

    private final VersionRepository versionRepository;

    public VersionResponse getVersion(String type) {
        Version version = versionRepository.getByType(VersionType.from(type));
        return VersionResponse.from(version);
    }
}
