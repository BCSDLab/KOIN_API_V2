package in.koreatech.koin.domain.order.shop.service;

import static in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestDeliveryOption.CAMPUS;
import static in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestDeliveryOption.OFF_CAMPUS;
import static in.koreatech.koin.global.code.ApiResponseCode.DUPLICATE_ORDERABLE_SHOP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import in.koreatech.koin.domain.order.shop.model.entity.delivery.OrderableShopDeliveryOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuGroup;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuGroupMap;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuImage;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOptionGroup;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOptionGroupMap;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShopImage;
import in.koreatech.koin.domain.order.shop.model.entity.shop.ShopOperation;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import in.koreatech.koin.domain.order.shop.repository.ShopOperationRepository;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceMenuSnapshot;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.domain.shop.cache.aop.RefreshShopsCache;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopImage;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderableShopPromotionService {

    private final ShopRepository shopRepository;
    private final OrderableShopRepository orderableShopRepository;
    private final ShopOperationRepository shopOperationRepository;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.MANDATORY)
    @RefreshShopsCache
    public void promote(ShopOrderServiceRequest request) {
        Shop shop = shopRepository.getByIdForUpdate(request.getShop().getId());
        if (orderableShopRepository.countByShopIdIncludingDeleted(shop.getId()) > 0) {
            throw CustomException.of(DUPLICATE_ORDERABLE_SHOP, "shopId: " + shop.getId());
        }

        OrderableShop orderableShop = orderableShopRepository.save(OrderableShop.builder()
            .shop(shop)
            .delivery(true)
            .takeout(request.getIsTakeout())
            .minimumOrderAmount(request.getMinimumOrderAmount())
            .menuGroups(new ArrayList<>())
            .build());
        entityManager.persist(OrderableShopDeliveryOption.builder()
            .orderableShop(orderableShop)
            .campusDelivery(request.getDeliveryOption() != OFF_CAMPUS)
            .offCampusDelivery(request.getDeliveryOption() != CAMPUS)
            .build());

        prepareShopOperation(shop);
        shop.enableOrderServiceBankAccount(request.getBank(), request.getAccountNumber(), request.getAccountHolder());
        copyShopImages(orderableShop, shop.getShopImages());
        transferMenus(orderableShop, request.getMenus());
    }

    private void prepareShopOperation(Shop shop) {
        shopOperationRepository.findByShopIdIncludingDeleted(shop.getId()).ifPresentOrElse(
            ShopOperation::prepareForOrderService,
            () -> shopOperationRepository.save(ShopOperation.builder().shop(shop).build())
        );
    }

    private void copyShopImages(OrderableShop orderableShop, List<ShopImage> sourceImages) {
        List<String> imageUrls = sourceImages.stream()
            .sorted(Comparator.comparing(ShopImage::getId))
            .map(ShopImage::getImageUrl)
            .filter(StringUtils::hasText)
            .toList();
        for (int index = 0; index < imageUrls.size(); index++) {
            OrderableShopImage image = OrderableShopImage.builder()
                .orderableShop(orderableShop)
                .imageUrl(imageUrls.get(index))
                .isThumbnail(index == 0)
                .build();
            orderableShop.getShopImages().add(image);
            entityManager.persist(image);
        }
    }

    private void transferMenus(OrderableShop orderableShop, List<ShopOrderServiceMenuSnapshot> menus) {
        Map<Integer, OrderableShopMenuGroup> groupsBySourceId = new HashMap<>();
        for (ShopOrderServiceMenuSnapshot snapshot : menus) {
            OrderableShopMenu menu = createMenu(orderableShop, snapshot);
            if (snapshot.categories().isEmpty()) {
                OrderableShopMenuGroup group = groupsBySourceId.computeIfAbsent(
                    null, ignored -> createMenuGroup(orderableShop, "기타")
                );
                addMenuToGroup(menu, group);
            }
            for (ShopOrderServiceMenuSnapshot.Category category : snapshot.categories()) {
                OrderableShopMenuGroup group = groupsBySourceId.computeIfAbsent(
                    category.id(), ignored -> createMenuGroup(orderableShop, category.name())
                );
                addMenuToGroup(menu, group);
            }
        }
    }

    private OrderableShopMenuGroup createMenuGroup(OrderableShop orderableShop, String name) {
        OrderableShopMenuGroup group = OrderableShopMenuGroup.builder()
            .orderableShop(orderableShop)
            .name(name)
            .build();
        orderableShop.getMenuGroups().add(group);
        entityManager.persist(group);
        return group;
    }

    private void addMenuToGroup(OrderableShopMenu menu, OrderableShopMenuGroup group) {
        OrderableShopMenuGroupMap menuGroupMap = OrderableShopMenuGroupMap.builder()
            .menuGroup(group)
            .menu(menu)
            .build();
        group.getMenuGroupMap().add(menuGroupMap);
        entityManager.persist(menuGroupMap);
    }

    private OrderableShopMenu createMenu(OrderableShop orderableShop, ShopOrderServiceMenuSnapshot snapshot) {
        OrderableShopMenu menu = OrderableShopMenu.builder()
            .orderableShop(orderableShop)
            .name(snapshot.name())
            .description(snapshot.description())
            .isSoldOut(false)
            .isDeleted(false)
            .menuPrices(new ArrayList<>())
            .menuImages(new ArrayList<>())
            .menuOptionGroupMap(new ArrayList<>())
            .build();

        for (ShopOrderServiceMenuSnapshot.Price price : snapshot.prices()) {
            menu.getMenuPrices().add(OrderableShopMenuPrice.builder()
                .menu(menu)
                .name(price.name())
                .price(price.price())
                .isDeleted(false)
                .build());
        }
        for (int index = 0; index < snapshot.imageUrls().size(); index++) {
            menu.getMenuImages().add(OrderableShopMenuImage.builder()
                .menu(menu)
                .imageUrl(snapshot.imageUrls().get(index))
                .isThumbnail(index == 0)
                .build());
        }
        entityManager.persist(menu);
        for (ShopOrderServiceMenuSnapshot.OptionGroup optionGroup : snapshot.optionGroups()) {
            createMenuOptionGroup(orderableShop, menu, optionGroup);
        }
        return menu;
    }

    private void createMenuOptionGroup(
        OrderableShop orderableShop,
        OrderableShopMenu menu,
        ShopOrderServiceMenuSnapshot.OptionGroup snapshot
    ) {
        OrderableShopMenuOptionGroup group = OrderableShopMenuOptionGroup.builder()
            .orderableShop(orderableShop)
            .name(snapshot.name())
            .description(snapshot.description())
            .isRequired(snapshot.isRequired())
            .minSelect(snapshot.minSelect())
            .maxSelect(snapshot.maxSelect())
            .isDeleted(false)
            .menuOptions(new ArrayList<>())
            .build();
        for (ShopOrderServiceMenuSnapshot.Option option : snapshot.options()) {
            group.addMenuOption(OrderableShopMenuOption.builder()
                .optionGroup(group)
                .name(option.name())
                .price(option.price())
                .isDeleted(false)
                .build());
        }
        entityManager.persist(group);
        OrderableShopMenuOptionGroupMap map = OrderableShopMenuOptionGroupMap.builder()
            .optionGroup(group)
            .menu(menu)
            .isDeleted(false)
            .build();
        menu.getMenuOptionGroupMap().add(map);
        entityManager.persist(map);
    }
}
