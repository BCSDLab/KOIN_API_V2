package in.koreatech.koin.admin.coopShop.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import in.koreatech.koin.admin.coopShop.dto.AdminCoopSemesterResponse;
import in.koreatech.koin.admin.coopShop.dto.AdminCoopSemestersResponse;
import in.koreatech.koin.admin.coopShop.dto.AdminUpdateSemesterRequest;
import in.koreatech.koin.admin.coopShop.dto.AdminUpdateSemesterRequest.InnerCoopShop;
import in.koreatech.koin.admin.coopShop.dto.AdminUpdateSemesterRequest.InnerCoopShop.InnerOperationHour;
import in.koreatech.koin.admin.coopShop.repository.AdminCoopNameRepository;
import in.koreatech.koin.admin.coopShop.repository.AdminCoopSemesterRepository;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.coopshop.model.CoopName;
import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCoopShopService {

    private final AdminCoopSemesterRepository adminCoopSemesterRepository;
    private final AdminCoopNameRepository adminCoopNameRepository;

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

    public void updateCoopShops(AdminUpdateSemesterRequest request) {
        CoopSemester coopSemester = adminCoopSemesterRepository.getById(request.semesterId());
        List<CoopShop> coopShops = request.coopShops().stream()
            .map(this::createCoopShop)
            .toList();
        coopSemester.updateCoopShops(coopShops);
    }

    private CoopShop createCoopShop(InnerCoopShop innerCoopShop) {
        CoopName coopName = findOrCreateCoopName(innerCoopShop.coopShopInfo().name());
        CoopShop coopShop = innerCoopShop.toEntity(coopName);
        attachOperationHours(innerCoopShop, coopShop);
        return coopShop;
    }

    private CoopName findOrCreateCoopName(String name) {
        return adminCoopNameRepository.findByName(name)
            .orElse(CoopName.builder()
                .name(name)
                .build());
    }

    private void attachOperationHours(InnerCoopShop innerCoopShop, CoopShop coopShop) {
        innerCoopShop.operationHours().stream()
            .map(InnerOperationHour::toEntity)
            .forEach(coopOpen -> coopOpen.confirmCoopShop(coopShop));
    }
}
