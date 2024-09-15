package in.koreatech.koin.admin.version.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import in.koreatech.koin.admin.version.dto.AdminVersionResponse;
import in.koreatech.koin.admin.version.repository.AdminVersionRepository;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminVersionService {

    private final AdminVersionRepository adminVersionRepository;

    public AdminVersionResponse getVersion(Integer page, Integer limit) {
        Integer total = adminVersionRepository.countAll();

        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id"));

        Page<Version> result = adminVersionRepository.findAll(pageRequest);

        return null;
    }

    public AdminVersionResponse getVersions(String type, Integer page, Integer limit) {
        Integer total = adminVersionRepository.countAll();

        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id"));

        Page<Version> result = adminVersionRepository.findAllByType(type, pageRequest);

        return null;
    }
}
