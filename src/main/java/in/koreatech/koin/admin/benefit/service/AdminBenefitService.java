package in.koreatech.koin.admin.benefit.service;

import java.util.List;
import java.util.Map;
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
import in.koreatech.koin.admin.benefit.dto.AdminModifyBenefitShopsRequest;
import in.koreatech.koin.admin.benefit.dto.AdminSearchBenefitShopsResponse;
import in.koreatech.koin.admin.benefit.exception.BenefitDuplicationException;
import in.koreatech.koin.admin.benefit.exception.BenefitLimitException;
import in.koreatech.koin.admin.benefit.exception.BenefitMapNotFoundException;
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

        boolean existsCategory = adminBenefitCategoryRepository.existsByTitle(request.title());
        if (existsCategory) {
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
        boolean existsCategory = adminBenefitCategoryRepository.existsByTitleAndIdNot(request.title(), categoryId);
        if (existsCategory) {
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
        BenefitCategory benefitCategory = adminBenefitCategoryRepository.getById(benefitId);
        Map<Integer, String> shopIdToDetail = request.shopDetails().stream()
            .collect(Collectors.toMap(
                AdminCreateBenefitShopsRequest.InnerBenefitShopsRequest::shopId,
                AdminCreateBenefitShopsRequest.InnerBenefitShopsRequest::detail
            ));
        List<Shop> shops = adminShopRepository.findAllByIdIn(shopIdToDetail.keySet().stream().toList());

        List<BenefitCategoryMap> benefitCategoryMaps = shops.stream()
            .map(shop -> BenefitCategoryMap.builder()
                .shop(shop)
                .benefitCategory(benefitCategory)
                .detail(shopIdToDetail.get(shop.getId()))
                .build()
            )
            .toList();
        adminBenefitCategoryMapRepository.saveAll(benefitCategoryMaps);
        return AdminCreateBenefitShopsResponse.from(benefitCategoryMaps);
    }

    @Transactional
    public void modifyBenefitShops(AdminModifyBenefitShopsRequest request) {
        Map<Integer, String> shopBenefitIdToDetail = request.modifyDetails().stream()
            .collect(Collectors.toMap(
                AdminModifyBenefitShopsRequest.InnerBenefitShopsRequest::shopBenefitMapId,
                AdminModifyBenefitShopsRequest.InnerBenefitShopsRequest::detail
            ));

        List<BenefitCategoryMap> benefitCategoryMaps =
            adminBenefitCategoryMapRepository.findAllByIdIn(shopBenefitIdToDetail.keySet().stream().toList());

        validateBenefitMapIds(shopBenefitIdToDetail, benefitCategoryMaps);
        benefitCategoryMaps.forEach(map -> map.modifyDetail(shopBenefitIdToDetail.get(map.getId())));
    }

    private static void validateBenefitMapIds(
        Map<Integer, String> shopBenefitIdToDetail,
        List<BenefitCategoryMap> benefitCategoryMaps
    ) {
        List<Integer> notFoundMapIds = shopBenefitIdToDetail.keySet().stream()
            .filter(mapId -> benefitCategoryMaps.stream().noneMatch(map -> map.getId().equals(mapId)))
            .toList();

        if (!notFoundMapIds.isEmpty()) {
            throw new BenefitMapNotFoundException("해당 혜택 카테고리에 존재하지 않는 상점이 포함되어 있습니다. shopBenefitMapId: " + notFoundMapIds);
        }
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

