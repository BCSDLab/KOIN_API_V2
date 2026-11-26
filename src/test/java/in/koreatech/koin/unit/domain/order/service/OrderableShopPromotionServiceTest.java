package in.koreatech.koin.unit.domain.order.service;

import static in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestDeliveryOption.CAMPUS;
import static in.koreatech.koin.global.code.ApiResponseCode.DUPLICATE_ORDERABLE_SHOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.order.shop.model.entity.delivery.ShopBaseDeliveryTip;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import in.koreatech.koin.domain.order.shop.repository.ShopOperationRepository;
import in.koreatech.koin.domain.order.shop.service.OrderableShopPromotionService;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceMenuSnapshot;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixture.OwnerFixture;
import in.koreatech.koin.unit.fixture.ShopFixture;
import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class OrderableShopPromotionServiceTest {

    @InjectMocks
    private OrderableShopPromotionService orderableShopPromotionService;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private OrderableShopRepository orderableShopRepository;

    @Mock
    private ShopOperationRepository shopOperationRepository;

    @Mock
    private EntityManager entityManager;

    private Shop shop;
    private ShopOrderServiceRequest application;

    @BeforeEach
    void setUp() {
        shop = ShopFixture.주문전환_이전_상점(OwnerFixture.성빈_사장님());
        ShopOrderServiceMenuSnapshot menu = new ShopOrderServiceMenuSnapshot(
            101, "탕수육", null,
            List.of(new ShopOrderServiceMenuSnapshot.Category(1, "메인")),
            List.of(new ShopOrderServiceMenuSnapshot.Price("소", 18000),
                new ShopOrderServiceMenuSnapshot.Price("대", 26000)),
            List.of(),
            List.of(new ShopOrderServiceMenuSnapshot.OptionGroup(
                "토핑 추가", null, false, 0, 1,
                List.of(new ShopOrderServiceMenuSnapshot.Option("치즈 추가", 1000))
            ))
        );
        application = ShopOrderServiceRequest.builder()
            .shop(shop)
            .isTakeout(true)
            .minimumOrderAmount(15000)
            .deliveryOption(CAMPUS)
            .campusDeliveryTip(1000)
            .offCampusDeliveryTip(2000)
            .bank("국민은행")
            .accountNumber("123-456-789")
            .accountHolder("홍길동")
            .menus(List.of(menu))
            .build();
    }

    @Test
    @DisplayName("승격 시 신청의 메뉴 가격과 추가 옵션을 구분해 이관하고 기존 배달팁을 유지한다")
    void transferMenuPricesAndOptionsWithoutChangingDeliveryTips() {
        ShopBaseDeliveryTip existingTip = mock(ShopBaseDeliveryTip.class);
        when(existingTip.getOrderAmount()).thenReturn(0);
        when(existingTip.getFee()).thenReturn(2200);
        shop.getBaseDeliveryTips().getBaseDeliveryTips().add(existingTip);
        when(shopRepository.getByIdForUpdate(shop.getId())).thenReturn(shop);
        when(orderableShopRepository.save(any(OrderableShop.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderableShopPromotionService.promote(application);

        ArgumentCaptor<OrderableShop> captor = ArgumentCaptor.forClass(OrderableShop.class);
        verify(orderableShopRepository).save(captor.capture());
        OrderableShop orderableShop = captor.getValue();
        var menu = orderableShop.getMenuGroups().get(0).getMenuGroupMap().get(0).getMenu();

        assertThat(menu.getName()).isEqualTo("탕수육");
        assertThat(menu.getMenuPrices()).extracting(OrderableShopMenuPrice::getPrice).containsExactly(18000, 26000);
        assertThat(menu.getMenuOptionGroupMap()).singleElement().satisfies(map -> {
            assertThat(map.getOptionGroup().getName()).isEqualTo("토핑 추가");
            assertThat(map.getOptionGroup().getMenuOptions()).extracting(OrderableShopMenuOption::getPrice)
                .containsExactly(1000);
        });
        assertThat(orderableShop.calculateDeliveryFee(15000)).isEqualTo(2200);
    }

    @Test
    @DisplayName("이미 주문 가능 상점인 경우 새 상점과 메뉴를 저장하지 않는다")
    void cannotPromoteExistingOrderableShop() {
        when(shopRepository.getByIdForUpdate(shop.getId())).thenReturn(shop);
        when(orderableShopRepository.countByShopIdIncludingDeleted(shop.getId())).thenReturn(1L);

        assertThatThrownBy(() -> orderableShopPromotionService.promote(application))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(DUPLICATE_ORDERABLE_SHOP);

        verify(orderableShopRepository, never()).save(any(OrderableShop.class));
        verifyNoInteractions(shopOperationRepository, entityManager);
    }
}
