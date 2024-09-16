package in.koreatech.koin.domain.mobileversion.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.mobileversion.model.MobileVersion;
import in.koreatech.koin.domain.mobileversion.repository.MobileVersionRepository;
import in.koreatech.koin.domain.mobileversion.dto.MobileVersionResponse;
import in.koreatech.koin.domain.version.model.VersionType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MobileVersionService {

    private final MobileVersionRepository mobileVersionRepository;

    public MobileVersionResponse getVersion(String type) {
        MobileVersion version = mobileVersionRepository.getByType(VersionType.from(type));
        return MobileVersionResponse.from(version);
    }
}
