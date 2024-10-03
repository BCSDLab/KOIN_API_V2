package in.koreatech.koin.admin.coopShop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import in.koreatech.koin.admin.coopShop.dto.AdminCoopShopSemesterResponse;
import in.koreatech.koin.admin.coopShop.dto.AdminCoopShopSemestersResponse;
import in.koreatech.koin.admin.coopShop.repository.AdminCoopShopSemesterRepository;
import in.koreatech.koin.domain.coopshop.model.CoopShopSemester;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCoopShopService {

    private final AdminCoopShopSemesterRepository adminCoopShopSemesterRepository;

    public AdminCoopShopSemestersResponse getCoopShopSemesters(Integer page, Integer limit) {
        Integer total = adminCoopShopSemesterRepository.count();

        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id"));

        Page<CoopShopSemester> result = adminCoopShopSemesterRepository.findAll(pageRequest);

        return AdminCoopShopSemestersResponse.of(result, criteria);
    }

    public AdminCoopShopSemesterResponse getCoopShopSemester(Integer id) {
        CoopShopSemester coopShopSemester = adminCoopShopSemesterRepository.getById(id);
        return AdminCoopShopSemesterResponse.from(coopShopSemester);
    }
}
