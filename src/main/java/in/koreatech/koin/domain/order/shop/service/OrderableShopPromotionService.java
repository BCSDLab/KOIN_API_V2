package in.koreatech.koin.domain.order.shop.service;

import static in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestDeliveryOption.*;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.shop.model.entity.delivery.OrderableShopDeliveryOption;
import in.koreatech.koin.domain.order.shop.model.entity.delivery.ShopBaseDeliveryTip;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuGroup;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuGroupMap;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuImage;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShopImage;
import in.koreatech.koin.domain.order.shop.model.entity.shop.ShopOperation;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceTransferData;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderableShopPromotionService {

    private final ShopRepository shopRepository;
    private final OrderableShopRepository orderableShopRepository;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.MANDATORY)
    public void promote(ShopOrderServiceRequest request) {
        request.requirePending();
        Shop shop = shopRepository.getByIdForUpdate(request.getShop().getId());
        if (orderableShopRepository.existsByShopId(shop.getId())) {
            throw CustomException.of(DUPLICATE_ORDERABLE_SHOP);
        }
        if (request.getTransferData() == null || request.getDeliveryTip() == null) {
            throw CustomException.of(ORDER_SERVICE_TRANSFER_DATA_REQUIRED);
        }
        OrderableShop orderableShop = OrderableShop.builder()
            .shop(shop)
            .delivery(true)
            .takeout(request.getIsTakeout())
            .minimumOrderAmount(request.getMinimumOrderAmount())
            .menuGroups(new ArrayList<>())
            .build();
        entityManager.persist(orderableShop);
        entityManager.persist(new OrderableShopDeliveryOption(orderableShop,
            request.getDeliveryOption() != OFF_CAMPUS, request.getDeliveryOption() != CAMPUS));

        if (shop.getShopOperation() == null) {
            entityManager.persist(ShopOperation.builder().shop(shop).isOpen(false).build());
        } else {
            shop.getShopOperation().changeOpenStatus(false);
        }
        shop.changeOrderServiceAccount(request.getBank(), request.getAccountNumber());
        shop.getBaseDeliveryTips().getBaseDeliveryTips().clear();
        shop.getBaseDeliveryTips().getBaseDeliveryTips().add(new ShopBaseDeliveryTip(shop, 0, request.getDeliveryTip()));
        transferMenus(orderableShop, request.getTransferData());
        for (int index = 0; index < request.getTransferData().imageUrls().size(); index++) {
            OrderableShopImage image = new OrderableShopImage(orderableShop,
                request.getTransferData().imageUrls().get(index), index == 0);
            orderableShop.getShopImages().add(image);
            entityManager.persist(image);
        }
    }

    private void transferMenus(OrderableShop shop, ShopOrderServiceTransferData data) {
        Map<Integer, OrderableShopMenu> transferredMenus = new HashMap<>();
        for (ShopOrderServiceTransferData.Category category : data.menuCategories()) {
            OrderableShopMenuGroup group = new OrderableShopMenuGroup(shop, category.name());
            shop.getMenuGroups().add(group);
            entityManager.persist(group);
            for (ShopOrderServiceTransferData.Menu menuData : category.menus()) {
                OrderableShopMenu menu = transferredMenus.computeIfAbsent(menuData.id(), id -> createMenu(shop, menuData));
                OrderableShopMenuGroupMap map = new OrderableShopMenuGroupMap(group, menu);
                group.getMenuGroupMap().add(map);
                entityManager.persist(map);
            }
        }
    }

    private OrderableShopMenu createMenu(OrderableShop shop, ShopOrderServiceTransferData.Menu data) {
        OrderableShopMenu menu = OrderableShopMenu.builder()
            .orderableShop(shop).name(data.name()).description(data.description())
            .isSoldOut(false).isDeleted(false)
            .menuPrices(new ArrayList<>()).menuImages(new ArrayList<>()).menuOptionGroupMap(new ArrayList<>())
            .build();
        entityManager.persist(menu);
        if (data.isSingle()) {
            OrderableShopMenuPrice price = OrderableShopMenuPrice.builder()
                .menu(menu).price(data.singlePrice()).isDeleted(false).build();
            menu.getMenuPrices().add(price);
            entityManager.persist(price);
        } else {
            for (ShopOrderServiceTransferData.OptionPrice price : data.optionPrices()) {
                OrderableShopMenuPrice menuPrice = OrderableShopMenuPrice.builder()
                    .menu(menu).name(price.option()).price(price.price()).isDeleted(false).build();
                menu.getMenuPrices().add(menuPrice);
                entityManager.persist(menuPrice);
            }
        }
        for (int index = 0; index < data.imageUrls().size(); index++) {
            OrderableShopMenuImage image = new OrderableShopMenuImage(menu, data.imageUrls().get(index), index == 0);
            menu.getMenuImages().add(image);
            entityManager.persist(image);
        }
        return menu;
    }
}
