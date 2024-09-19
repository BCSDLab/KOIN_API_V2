package in.koreatech.koin.admin.benefit.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.benefit.dto.AdminBenefitCategoryResponse;
import in.koreatech.koin.admin.benefit.dto.AdminBenefitShopsResponse;
import in.koreatech.koin.admin.benefit.dto.AdminCreateBenefitCategoryRequest;
import in.koreatech.koin.admin.benefit.dto.AdminCreateBenefitCategoryResponse;
import in.koreatech.koin.admin.benefit.dto.AdminCreateBenefitShopsRequest;
import in.koreatech.koin.admin.benefit.dto.AdminCreateBenefitShopsResponse;
import in.koreatech.koin.admin.benefit.dto.AdminDeleteShopsRequest;
import in.koreatech.koin.admin.benefit.dto.AdminSearchBenefitShopsResponse;
import in.koreatech.koin.admin.benefit.repository.AdminBenefitCategoryMapRepository;
import in.koreatech.koin.admin.benefit.repository.AdminBenefitCategoryRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopRepository;
import in.koreatech.koin.domain.benefit.model.BenefitCategory;
import in.koreatech.koin.domain.benefit.model.BenefitCategoryMap;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminBenefitService {

    private final AdminBenefitCategoryRepository adminBenefitCategoryRepository;
    private final AdminBenefitCategoryMapRepository adminBenefitCategoryMapRepository;
    private final AdminShopRepository adminShopRepository;

    public AdminBenefitCategoryResponse getBenefitCategories() {
        List<BenefitCategory> categories = adminBenefitCategoryRepository.findAllByOrderByTitleAsc();
        return AdminBenefitCategoryResponse.from(categories);
    }

    @Transactional
    public AdminCreateBenefitCategoryResponse createBenefitCategory(AdminCreateBenefitCategoryRequest request) {
        BenefitCategory benefitCategory = request.toBenefitCategory();
        BenefitCategory savedBenefitCategory = adminBenefitCategoryRepository.save(benefitCategory);
        return AdminCreateBenefitCategoryResponse.from(savedBenefitCategory);
    }

    @Transactional
    public void deleteBenefitCategory(Integer categoryId) {
        adminBenefitCategoryRepository.deleteById(categoryId);
    }

    public AdminBenefitShopsResponse getBenefitShops(Integer benefitId) {
        List<BenefitCategoryMap> benefitCategoryMaps = adminBenefitCategoryMapRepository.findAllByBenefitCategoryId(
            benefitId
        );
        List<Shop> shops = benefitCategoryMaps.stream()
            .map(BenefitCategoryMap::getShop)
            .toList();
        return AdminBenefitShopsResponse.from(shops);
    }

    @Transactional
    public AdminCreateBenefitShopsResponse createBenefitShops(
        Integer benefitId,
        AdminCreateBenefitShopsRequest request
    ) {
        List<Shop> shops = adminShopRepository.findAllByIdIn(request.shopIds());
        BenefitCategory benefitCategory = adminBenefitCategoryRepository.getById(benefitId);
        for (Shop shop : shops) {
            BenefitCategoryMap benefitCategoryMap = BenefitCategoryMap.builder()
                .shop(shop)
                .benefitCategory(benefitCategory)
                .build();
            adminBenefitCategoryMapRepository.save(benefitCategoryMap);
        }
        return AdminCreateBenefitShopsResponse.from(shops);
    }

    @Transactional
    public void deleteBenefitShops(Integer benefitId, AdminDeleteShopsRequest request) {
        adminBenefitCategoryMapRepository.deleteByBenefitCategoryIdAndShopIds(benefitId, request.shopIds());
    }

    public AdminSearchBenefitShopsResponse searchShops(Integer benefitId, String query) {
        return null;
    }
}
