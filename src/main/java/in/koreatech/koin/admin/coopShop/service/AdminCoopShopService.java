package in.koreatech.koin.admin.coopShop.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional(readOnly = true)
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

    @Transactional
    public void updateCoopShops(Integer semesterId, AdminUpdateSemesterRequest request) {
        CoopSemester coopSemester = adminCoopSemesterRepository.getById(semesterId);

        List<CoopShop> coopShops = request.coopShops().stream()
            .map(this::createCoopShop)
            .toList();

        coopSemester.replaceCoopShops(coopShops);
    }

    private CoopShop createCoopShop(InnerCoopShop innerCoopShop) {
        CoopName coopName = adminCoopNameRepository.findByName(innerCoopShop.coopShopInfo().name())
            .orElseGet(() -> CoopName.builder()
                .name(innerCoopShop.coopShopInfo().name())
                .build());
        adminCoopNameRepository.save(coopName);

        CoopShop coopShop = innerCoopShop.toEntity(coopName);

        innerCoopShop.operationHours().stream()
            .map(InnerOperationHour::toEntity)
            .forEach(coopOpen -> coopOpen.confirmCoopShop(coopShop));

        return coopShop;
    }
}
