package in.koreatech.koin.admin.land.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.land.dto.AdminLandsRequest;
import in.koreatech.koin.admin.land.dto.AdminLandsResponse;
import in.koreatech.koin.admin.land.execption.LandNameDuplicationException;
import in.koreatech.koin.admin.land.repository.AdminLandRepository;
import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLandService {

    private final AdminLandRepository adminLandRepository;

    public AdminLandsResponse getLands(Integer page, Integer limit, Boolean isDeleted) {

        // page > totalPage인 경우 totalPage로 조회하기 위해
        Integer total = adminLandRepository.countAllByIsDeleted(isDeleted);

        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id"));

        Page<Land> result = adminLandRepository.findAllByIsDeleted(isDeleted, pageRequest);

        return AdminLandsResponse.of(result, criteria);
    }

    @Transactional
    public void createLands(AdminLandsRequest adminLandsRequest) {

        adminLandRepository.getByName(adminLandsRequest.name());
        Land land = adminLandsRequest.toLand();
        adminLandRepository.save(land);
    }
}
