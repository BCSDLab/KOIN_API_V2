package in.koreatech.koin.domain.ownershop.service;

import static in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus.*;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.common.event.ShopOrderServiceRequestCreatedEvent;
import in.koreatech.koin.domain.ownershop.dto.ShopOrderServiceRequestRequest;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceMenuSnapshot;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.domain.ownershop.repository.ShopOrderServiceRequestRepository;
import in.koreatech.koin.domain.shop.model.menu.Menu;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShopOrderServiceRequestService {

    private final ShopOrderServiceRequestRepository shopOrderServiceRequestRepository;
    private final ShopRepository shopRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createOrderableRequest(Integer ownerId, ShopOrderServiceRequestRequest request, Integer shopId) {
        Shop shop = shopRepository.getById(shopId);

        validateShopOwner(shop, ownerId);
        validateDuplicateRequest(shop);
        List<ShopOrderServiceMenuSnapshot> menus = snapshotMenus(shop, request.menus());

        ShopOrderServiceRequest shopOrderServiceRequest = ShopOrderServiceRequest.builder()
            .shop(shop)
            .minimumOrderAmount(request.minimumOrderAmount())
            .isTakeout(request.isTakeout())
            .deliveryOption(request.deliveryOption())
            .campusDeliveryTip(request.campusDeliveryTip())
            .offCampusDeliveryTip(request.offCampusDeliveryTip())
            .businessLicenseUrl(request.businessLicenseUrl())
            .businessCertificateUrl(request.businessCertificateUrl())
            .bankCopyUrl(request.bankCopyUrl())
            .bank(request.bank())
            .accountNumber(request.accountNumber())
            .accountHolder(request.accountHolder())
            .menus(menus)
            .build();

        shopOrderServiceRequestRepository.save(shopOrderServiceRequest);
        eventPublisher.publishEvent(new ShopOrderServiceRequestCreatedEvent(shop.getName()));
    }

    private List<ShopOrderServiceMenuSnapshot> snapshotMenus(
        Shop shop,
        List<ShopOrderServiceRequestRequest.MenuSelection> selections
    ) {
        Map<Integer, Menu> menusById = shop.getMenus().stream()
            .collect(Collectors.toMap(Menu::getId, Function.identity()));

        return selections.stream()
            .map(selection -> {
                Menu menu = menusById.get(selection.menuId());
                if (menu == null) {
                    throw CustomException.of(INVALID_MENU_IN_SHOP, "menuId: " + selection.menuId());
                }
                if (menu.isHidden() || menu.getMenuOptions().isEmpty()) {
                    throw CustomException.of(ILLEGAL_ARGUMENT, "가격이 등록된 공개 메뉴만 이관할 수 있습니다.");
                }
                List<ShopOrderServiceMenuSnapshot.OptionGroup> optionGroups = selection.optionGroups().stream()
                    .map(ShopOrderServiceRequestService::snapshotOptionGroup)
                    .toList();
                return ShopOrderServiceMenuSnapshot.from(menu, optionGroups);
            })
            .toList();
    }

    private static ShopOrderServiceMenuSnapshot.OptionGroup snapshotOptionGroup(
        ShopOrderServiceRequestRequest.OptionGroup group
    ) {
        return new ShopOrderServiceMenuSnapshot.OptionGroup(
            group.name(),
            group.description(),
            group.isRequired(),
            group.minSelect(),
            group.maxSelect(),
            group.options().stream()
                .map(option -> new ShopOrderServiceMenuSnapshot.Option(option.name(), option.price()))
                .toList()
        );
    }

    private void validateShopOwner(Shop shop, Integer ownerId) {
        // 가게 사장님인지 확인
        if (!shop.isOwner(ownerId)) {
            throw CustomException.of(FORBIDDEN_SHOP_OWNER, "ownerId: " + ownerId + ", shopId: " + shop.getId());
        }
    }

    private void validateDuplicateRequest(Shop shop) {
        // 이미 신청한 내역이 있는지 확인
        if (shopOrderServiceRequestRepository.existsByShopIdAndRequestStatus(shop.getId(), PENDING)) {
            throw CustomException.of(DUPLICATE_REQUESTED_ORDERABLE_SHOP, "shopId: " + shop.getId());
        }

        // 이미 주문가능 상점인지 확인
        if (shopOrderServiceRequestRepository.existsByShopIdAndRequestStatus(shop.getId(), APPROVED)) {
            throw CustomException.of(DUPLICATE_ORDERABLE_SHOP, "shopId: " + shop.getId());
        }
    }
}
