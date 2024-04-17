package in.koreatech.koin.domain.admin.land.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.admin.land.dto.AdminLandsResponse;
import in.koreatech.koin.domain.admin.land.repository.AdminLandRepository;
import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminLandService {

    private final AdminLandRepository adminLandRepository;

    @Transactional(readOnly = true)
    public AdminLandsResponse getLands(Long page, Long limit, Boolean isDeleted) {

        // page > totalPage인 경우 totalPage로 조회하기 위해
        Long total = adminLandRepository.countAllByIsDeleted(isDeleted);

        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), Sort.by(Sort.Direction.ASC, "id"));

        Page<Land> result = adminLandRepository.findAllByIsDeleted(isDeleted, pageRequest);

        return AdminLandsResponse.of(result, criteria);
    }
}
