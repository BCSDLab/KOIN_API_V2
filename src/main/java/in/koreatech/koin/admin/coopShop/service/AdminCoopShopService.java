package in.koreatech.koin.admin.coopShop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import in.koreatech.koin.admin.coopShop.dto.AdminCoopSemesterResponse;
import in.koreatech.koin.admin.coopShop.dto.AdminCoopSemestersResponse;
import in.koreatech.koin.admin.coopShop.repository.AdminCoopSemesterRepository;
import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import in.koreatech.koin._common.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCoopShopService {

    private final AdminCoopSemesterRepository adminCoopSemesterRepository;

    public AdminCoopSemestersResponse getCoopShopSemesters(Integer page, Integer limit) {
        Integer total = adminCoopSemesterRepository.count();

        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id"));

        Page<CoopSemester> result = adminCoopSemesterRepository.findAll(pageRequest);

        return AdminCoopSemestersResponse.of(result, criteria);
    }

    public AdminCoopSemesterResponse getCoopShopSemester(Integer id) {
        CoopSemester coopSemester = adminCoopSemesterRepository.getById(id);
        return AdminCoopSemesterResponse.from(coopSemester);
    }
}
