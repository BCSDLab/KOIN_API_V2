package in.koreatech.koin.unit.domain.ownershop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.order.shop.model.entity.shop.ShopOperation;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.ownershop.service.OwnerShopService;
import in.koreatech.koin.domain.ownershop.service.OwnerShopUtilService;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixture.OwnerFixture;
import in.koreatech.koin.unit.fixture.ShopFixture;

@ExtendWith(MockitoExtension.class)
class OwnerShopOpenStatusServiceTest {

    private static final Integer OWNER_ID = 100;
    private static final Integer OTHER_OWNER_ID = 999;
    private static final Integer SHOP_ID = 100;

    @InjectMocks
    private OwnerShopService ownerShopService;

    @Mock
    private OwnerShopUtilService ownerShopUtilService;

    private Shop shop;

    @BeforeEach
    void setUp() {
        Owner owner = OwnerFixture.성빈_사장님();
        ReflectionTestUtils.setField(owner, "id", OWNER_ID);
        shop = ShopFixture.주문전환_이전_상점(owner);
    }

    @Test
    @DisplayName("영업을 시작하면 영업 중으로 바뀐다")
    void 영업을_시작하면_영업_중으로_바뀐다() {
        ShopOperation shopOperation = 영업_상태(false);
        when(ownerShopUtilService.getOwnerShopById(SHOP_ID, OWNER_ID)).thenReturn(shop);

        ownerShopService.changeShopOpenStatus(OWNER_ID, SHOP_ID, true);

        assertThat(shopOperation.isOpen()).isTrue();
    }

    @Test
    @DisplayName("영업을 종료하면 영업 중이 아닌 상태로 바뀐다")
    void 영업을_종료하면_영업_중이_아닌_상태로_바뀐다() {
        ShopOperation shopOperation = 영업_상태(true);
        when(ownerShopUtilService.getOwnerShopById(SHOP_ID, OWNER_ID)).thenReturn(shop);

        ownerShopService.changeShopOpenStatus(OWNER_ID, SHOP_ID, false);

        assertThat(shopOperation.isOpen()).isFalse();
    }

    @Test
    @DisplayName("같은 상태로 다시 요청해도 그대로 유지된다")
    void 같은_상태로_다시_요청해도_그대로_유지된다() {
        ShopOperation shopOperation = 영업_상태(true);
        when(ownerShopUtilService.getOwnerShopById(SHOP_ID, OWNER_ID)).thenReturn(shop);

        ownerShopService.changeShopOpenStatus(OWNER_ID, SHOP_ID, true);

        assertThat(shopOperation.isOpen()).isTrue();
    }

    @Test
    @DisplayName("주문 가능 상점으로 설정되지 않았으면 변경할 수 없다")
    void 주문_가능_상점이_아니면_변경할_수_없다() {
        when(ownerShopUtilService.getOwnerShopById(SHOP_ID, OWNER_ID)).thenReturn(shop);

        assertThatThrownBy(() -> ownerShopService.changeShopOpenStatus(OWNER_ID, SHOP_ID, true))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP);
    }

    @Test
    @DisplayName("상점의 사장님이 아니면 변경할 수 없다")
    void 상점의_사장님이_아니면_변경할_수_없다() {
        when(ownerShopUtilService.getOwnerShopById(SHOP_ID, OTHER_OWNER_ID))
            .thenThrow(AuthorizationException.withDetail("ownerId: " + OTHER_OWNER_ID));

        assertThatThrownBy(() -> ownerShopService.changeShopOpenStatus(OTHER_OWNER_ID, SHOP_ID, true))
            .isInstanceOf(AuthorizationException.class);
    }

    private ShopOperation 영업_상태(boolean isOpen) {
        ShopOperation shopOperation = ShopOperation.builder()
            .shop(shop)
            .isOpen(isOpen)
            .isDeleted(false)
            .build();
        ReflectionTestUtils.setField(shop, "shopOperation", shopOperation);
        return shopOperation;
    }
}
