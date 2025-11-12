package in.koreatech.koin.admin.coopShop.service;

import static in.koreatech.koin.admin.coopShop.dto.AdminUpdateSemesterRequest.InnerCoopShop;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import in.koreatech.koin.admin.coopShop.dto.AdminCoopSemesterResponse;
import in.koreatech.koin.admin.coopShop.dto.AdminCoopSemestersResponse;
import in.koreatech.koin.admin.coopShop.dto.AdminUpdateSemesterRequest;
import in.koreatech.koin.admin.coopShop.repository.AdminCoopNameRepository;
import in.koreatech.koin.admin.coopShop.repository.AdminCoopSemesterRepository;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.coopshop.model.CoopName;
import in.koreatech.koin.domain.coopshop.model.CoopOpen;
import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.model.DayType;
import jakarta.validation.Valid;
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

    public void updateCoopShops(@Valid AdminUpdateSemesterRequest request) {
        CoopSemester coopSemester = adminCoopSemesterRepository.getById(request.semesterId());
        List<CoopShop> coopShops = new ArrayList<>();
        for (InnerCoopShop innerCoopShop : request.coopShops()) {
            CoopName coopName = adminCoopNameRepository.findByName(innerCoopShop.coopShopInfo().name())
                .orElse(CoopName.builder()
                    .name(innerCoopShop.coopShopInfo().name())
                    .build());
            List<CoopOpen> coopOpens = innerCoopShop.operationHours().stream()
                .map(innerOperationHour -> CoopOpen.builder()
                    .dayOfWeek(DayType.from(innerOperationHour.dayOfWeek()))
                    .type(innerOperationHour.type())
                    .openTime(innerOperationHour.openTime())
                    .closeTime(innerOperationHour.closeTime())
                    .build()
                ).toList();
            CoopShop coopShop = CoopShop.builder()
                .coopName(coopName)
                .phone(innerCoopShop.coopShopInfo().phone())
                .remarks(innerCoopShop.coopShopInfo().phone())
                .location(innerCoopShop.coopShopInfo().location())
                .build();
            coopShop.addAllCoopOpens(coopOpens);
        }
        coopSemester.updateCoopShops(coopShops);
    }
}
