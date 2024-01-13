package in.koreatech.koin.domain.version.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.version.dto.VersionCreateRequest;
import in.koreatech.koin.domain.version.dto.VersionListResponse;
import in.koreatech.koin.domain.version.dto.VersionResponse;
import in.koreatech.koin.domain.version.dto.VersionUpdateRequest;
import in.koreatech.koin.domain.version.exception.VersionException;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VersionService {

    private final VersionRepository versionRepository;

    @Transactional(readOnly = true)
    public VersionListResponse getVersions() {
        List<Version> versions = versionRepository.findAll();

        return VersionListResponse.from(versions);
    }

    @Transactional(readOnly = true)
    public VersionResponse getVersion(String type) {
        Version version = this.getVersionEntity(type);

        return VersionResponse.from(version);
    }

    @Transactional
    public VersionResponse createVersion(VersionCreateRequest versionCreateRequest) {
        versionRepository.findByType(versionCreateRequest.type()).ifPresent(version -> {
            throw VersionException.withDetail("이미 동일한 타입이 존재합니다.");
        });

        Version version = Version.from(versionCreateRequest);
        versionRepository.save(version);

        return VersionResponse.from(version);
    }

    @Transactional
    public VersionResponse updateVersion(VersionUpdateRequest versionUpdateRequest, String type) {
        Version version = this.getVersionEntity(type);
        version.update(versionUpdateRequest);

        return VersionResponse.from(version);
    }

    @Transactional
    public void deleteVersion(String type) {
        Version version = this.getVersionEntity(type);
        versionRepository.delete(version);
    }

    private Version getVersionEntity(String type) {
        return versionRepository.findByType(type).orElseThrow(() -> VersionException.withDetail("일치하는 타입이 존재하지 않습니다."));
    }
}
