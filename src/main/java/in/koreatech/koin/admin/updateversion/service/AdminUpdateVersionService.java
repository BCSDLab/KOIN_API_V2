package in.koreatech.koin.admin.updateversion.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.updateversion.dto.AdminUpdateHistoryResponse;
import in.koreatech.koin.admin.updateversion.dto.AdminUpdateVersionRequest;
import in.koreatech.koin.admin.updateversion.dto.AdminUpdateVersionResponse;
import in.koreatech.koin.admin.updateversion.dto.AdminUpdateVersionsResponse;
import in.koreatech.koin.admin.updateversion.repository.AdminUpdateHistoryRepository;
import in.koreatech.koin.admin.updateversion.repository.AdminUpdateVersionRepository;
import in.koreatech.koin.domain.updateversion.model.UpdateContent;
import in.koreatech.koin.domain.updateversion.model.UpdateVersionHistory;
import in.koreatech.koin.domain.updateversion.model.UpdateVersion;
import in.koreatech.koin.domain.updateversion.model.UpdateVersionType;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUpdateVersionService {

    private final AdminUpdateVersionRepository adminUpdateVersionRepository;
    private final AdminUpdateHistoryRepository adminUpdateHistoryRepository;

    public AdminUpdateVersionsResponse getVersions(Integer page, Integer limit) {
        Integer total = adminUpdateVersionRepository.count();

        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id"));

        Page<UpdateVersion> result = adminUpdateVersionRepository.findAll(pageRequest);

        return AdminUpdateVersionsResponse.of(result, criteria);
    }

    public AdminUpdateVersionResponse getVersion(UpdateVersionType type) {
        UpdateVersion result = adminUpdateVersionRepository.getByType(type);
        return AdminUpdateVersionResponse.from(result);
    }

    @Transactional
    public void updateVersion(UpdateVersionType type, AdminUpdateVersionRequest request) {
        UpdateVersion version = adminUpdateVersionRepository.getByType(type);
        version.getContents().clear();
        List<UpdateContent> contents = request.body().stream()
            .map(body -> body.toEntity(version))
            .toList();
        version.update(
            request.version(),
            request.title(),
            contents
        );
        adminUpdateVersionRepository.save(version);
        adminUpdateHistoryRepository.save(UpdateVersionHistory.from(version));
    }

    public AdminUpdateHistoryResponse getUpdateHistory(UpdateVersionType type, Integer page, Integer limit) {
        Integer total = adminUpdateHistoryRepository.countByType(type);

        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id"));

        Page<UpdateVersionHistory> result = adminUpdateHistoryRepository.findAllByType(type, pageRequest);

        return AdminUpdateHistoryResponse.of(result, criteria);
    }
}
