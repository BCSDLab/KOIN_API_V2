package in.koreatech.koin.admin.version.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.version.dto.AdminVersionHistoryResponse;
import in.koreatech.koin.admin.version.dto.AdminVersionResponse;
import in.koreatech.koin.admin.version.dto.AdminVersionUpdateRequest;
import in.koreatech.koin.admin.version.dto.AdminVersionsResponse;
import in.koreatech.koin.admin.version.repository.AdminVersionRepository;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminVersionService {

    private final AdminVersionRepository adminVersionRepository;

    public AdminVersionsResponse getVersions(Integer page, Integer limit) {
        Integer total = adminVersionRepository.countByIsPrevious(false);

        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id"));

        Page<Version> result = adminVersionRepository.findAllByIsPrevious(false, pageRequest);

        return AdminVersionsResponse.of(result, criteria);
    }

    public AdminVersionResponse getVersion(String type) {
        Version version = adminVersionRepository.getByTypeAndIsPrevious(VersionType.from(type), false);
        return AdminVersionResponse.from(version);
    }

    @Transactional
    public void updateVersion(String type, AdminVersionUpdateRequest request) {
        VersionType versionType = VersionType.from(type);
        if (isPlatform(versionType)) {
            throw new KoinIllegalArgumentException("unsupported type", "type: " + versionType);
        }

        Version currentVersion = adminVersionRepository.getByTypeAndIsPrevious(versionType, false);
        currentVersion.toPreviousVersion();

        Version newVersion = Version.of(versionType, request);
        adminVersionRepository.save(newVersion);
    }

    private static boolean isPlatform(VersionType versionType) {
        return versionType.getCategory().equals("PLATFORM");
    }

    public AdminVersionHistoryResponse getHistory(String type, Integer page, Integer limit) {
        VersionType versionType = VersionType.from(type);
        Integer total = adminVersionRepository.countByType(versionType.getValue());

        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id"));

        Page<Version> result = adminVersionRepository.findAllByType(versionType.getValue(), pageRequest);

        return AdminVersionHistoryResponse.of(result, criteria);
    }
}
