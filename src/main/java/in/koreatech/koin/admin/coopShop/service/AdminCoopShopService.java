package in.koreatech.koin.admin.coopShop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import in.koreatech.koin.admin.coopShop.dto.AdminCoopShopResponse;
import in.koreatech.koin.admin.coopShop.dto.AdminCoopShopsResponse;
import in.koreatech.koin.admin.coopShop.repository.AdminCoopShopRepository;
import in.koreatech.koin.domain.coopshop.dto.CoopShopResponse;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCoopShopService {

    private final AdminCoopShopRepository adminCoopShopRepository;

    public AdminCoopShopsResponse getCoopsShops(Integer page, Integer limit, Boolean isDeleted) {
        Integer total = adminCoopShopRepository.countAllByIsDeleted(isDeleted);

        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id"));

        Page<CoopShop> result = adminCoopShopRepository.findAllByIsDeleted(isDeleted, pageRequest);

        return AdminCoopShopsResponse.of(result, criteria);
    }

    public AdminCoopShopResponse getCoopShop(Integer id) {
        CoopShop coopShop = adminCoopShopRepository.getById(id);
        return AdminCoopShopResponse.from(coopShop);
    }
}
