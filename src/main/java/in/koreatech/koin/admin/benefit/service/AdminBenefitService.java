package in.koreatech.koin.admin.benefit.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.benefit.dto.AdminBenefitCategoriesResponse;
import in.koreatech.koin.admin.benefit.dto.AdminBenefitShopsResponse;
import in.koreatech.koin.admin.benefit.dto.AdminCreateBenefitCategoryRequest;
import in.koreatech.koin.admin.benefit.dto.AdminCreateBenefitCategoryResponse;
import in.koreatech.koin.admin.benefit.dto.AdminCreateBenefitShopsRequest;
import in.koreatech.koin.admin.benefit.dto.AdminCreateBenefitShopsResponse;
import in.koreatech.koin.admin.benefit.dto.AdminDeleteShopsRequest;
import in.koreatech.koin.admin.benefit.dto.AdminModifyBenefitCategoryRequest;
import in.koreatech.koin.admin.benefit.dto.AdminModifyBenefitCategoryResponse;
import in.koreatech.koin.admin.benefit.dto.AdminSearchBenefitShopsResponse;
import in.koreatech.koin.admin.benefit.exception.BenefitDuplicationException;
import in.koreatech.koin.admin.benefit.exception.BenefitLimitException;
import in.koreatech.koin.admin.benefit.repository.AdminBenefitCategoryMapRepository;
import in.koreatech.koin.admin.benefit.repository.AdminBenefitCategoryRepository;
import in.koreatech.koin.admin.shop.repository.shop.AdminShopRepository;
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

    public AdminBenefitCategoriesResponse getBenefitCategories() {
        List<BenefitCategory> categories = adminBenefitCategoryRepository.findAllByOrderByTitleAsc();
        return AdminBenefitCategoriesResponse.from(categories);
    }

    @Transactional
    public AdminCreateBenefitCategoryResponse createBenefitCategory(AdminCreateBenefitCategoryRequest request) {
        int currentCategoryCount = adminBenefitCategoryRepository.count();
        if (BenefitCategory.isExceededMaxCategoryCount(currentCategoryCount)) {
            throw BenefitLimitException.withDetail("최대 혜택 수는 " + BenefitCategory.MAX_BENEFIT_CATEGORIES + "개 입니다.");
        }

        boolean isExistCategory = adminBenefitCategoryRepository.existsByTitle(request.title());
        if (isExistCategory) {
            throw BenefitDuplicationException.withDetail("이미 존재하는 혜택 카테고리입니다.");
        }

        BenefitCategory benefitCategory = request.toBenefitCategory();
        BenefitCategory savedBenefitCategory = adminBenefitCategoryRepository.save(benefitCategory);
        return AdminCreateBenefitCategoryResponse.from(savedBenefitCategory);
    }

    @Transactional
    public AdminModifyBenefitCategoryResponse modifyBenefitCategory(
        Integer categoryId,
        AdminModifyBenefitCategoryRequest request
    ) {
        boolean isExistCategory = adminBenefitCategoryRepository.existsByTitleAndIdNot(request.title(), categoryId);
        if (isExistCategory) {
            throw BenefitDuplicationException.withDetail("이미 존재하는 혜택 카테고리입니다.");
        }
        BenefitCategory benefitCategory = adminBenefitCategoryRepository.getById(categoryId);
        benefitCategory.update(
            request.title(),
            request.detail(),
            request.onImageUrl(),
            request.offImageUrl()
        );
        return AdminModifyBenefitCategoryResponse.from(benefitCategory);
    }

    @Transactional
    public void deleteBenefitCategory(Integer categoryId) {
        int currentCategoryCount = adminBenefitCategoryRepository.count();
        if (BenefitCategory.isBelowMinCategoryCount(currentCategoryCount)) {
            throw BenefitLimitException.withDetail("최소 혜택 수는" + BenefitCategory.MIN_BENEFIT_CATEGORIES + "개 입니다.");
        }
        adminBenefitCategoryMapRepository.deleteByBenefitCategoryId(categoryId);
        adminBenefitCategoryRepository.deleteById(categoryId);
    }

    public AdminBenefitShopsResponse getBenefitShops(Integer benefitId) {
        List<BenefitCategoryMap> benefitCategoryMaps =
            adminBenefitCategoryMapRepository.findAllByBenefitCategoryIdOrderByShopName(benefitId);
        return AdminBenefitShopsResponse.from(benefitCategoryMaps);
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

    public AdminSearchBenefitShopsResponse searchShops(Integer benefitId, String searchKeyword) {
        List<Shop> shops = adminShopRepository.searchByName(searchKeyword);
        Set<Integer> benefitShopIds = adminBenefitCategoryMapRepository
                .findAllByBenefitCategoryIdOrderByShopName(benefitId).stream()
                .map(benefitCategoryMap -> benefitCategoryMap.getShop().getId())
                .collect(Collectors.toSet());
        List<Shop> benefitShops = shops.stream()
            .filter(shop -> benefitShopIds.contains(shop.getId()))
            .toList();
        List<Shop> nonBenefitShops = shops.stream()
            .filter(shop -> !benefitShopIds.contains(shop.getId()))
            .toList();
        return AdminSearchBenefitShopsResponse.from(benefitShops, nonBenefitShops);
    }
}

