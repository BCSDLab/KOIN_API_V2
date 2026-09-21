package in.koreatech.koin.unit.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.order.shop.dto.OwnerOrderableShopsResponse;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import in.koreatech.koin.domain.order.shop.service.OwnerOrderableShopService;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixture.OrderableShopFixture;
import in.koreatech.koin.unit.fixture.OwnerFixture;

@ExtendWith(MockitoExtension.class)
class OwnerOrderableShopServiceTest {

    private static final Integer OWNER_ID = 100;
    private static final Integer OTHER_OWNER_ID = 999;
    private static final Integer ORDERABLE_SHOP_ID = 1;

    @InjectMocks
    private OwnerOrderableShopService ownerOrderableShopService;

    @Mock
    private OrderableShopRepository orderableShopRepository;

    private OrderableShop orderableShop;

    @BeforeEach
    void setUp() {
        orderableShop = OrderableShopFixture.김밥천국(1);

        Owner owner = OwnerFixture.성빈_사장님();
        ReflectionTestUtils.setField(owner, "id", OWNER_ID);
        ReflectionTestUtils.setField(orderableShop.getShop(), "owner", owner);
    }

    @Test
    @DisplayName("영업을 종료하면 영업 중이 아닌 상태로 바뀐다")
    void 영업을_종료하면_영업_중이_아닌_상태로_바뀐다() {
        when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);

        ownerOrderableShopService.changeOpenStatus(OWNER_ID, ORDERABLE_SHOP_ID, false);

        assertThat(orderableShop.getShop().getShopOperation().isOpen()).isFalse();
    }

    @Test
    @DisplayName("영업을 시작하면 영업 중으로 바뀐다")
    void 영업을_시작하면_영업_중으로_바뀐다() {
        OrderableShop closedShop = 영업_종료된_상점();
        when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(closedShop);

        ownerOrderableShopService.changeOpenStatus(OWNER_ID, ORDERABLE_SHOP_ID, true);

        assertThat(closedShop.getShop().getShopOperation().isOpen()).isTrue();
    }

    @Test
    @DisplayName("같은 값으로 다시 요청해도 상태가 유지된다")
    void 같은_값으로_다시_요청해도_상태가_유지된다() {
        when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);

        ownerOrderableShopService.changeOpenStatus(OWNER_ID, ORDERABLE_SHOP_ID, true);
        ownerOrderableShopService.changeOpenStatus(OWNER_ID, ORDERABLE_SHOP_ID, true);

        assertThat(orderableShop.getShop().getShopOperation().isOpen()).isTrue();
    }

    @Test
    @DisplayName("영업 상태 정보가 없으면 변경할 수 없다")
    void 영업_상태_정보가_없으면_변경할_수_없다() {
        ReflectionTestUtils.setField(orderableShop.getShop(), "shopOperation", null);
        when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);

        assertThatThrownBy(() -> ownerOrderableShopService.changeOpenStatus(OWNER_ID, ORDERABLE_SHOP_ID, true))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP);
    }

    @Test
    @DisplayName("상점의 사장님이 아니면 변경할 수 없다")
    void 상점의_사장님이_아니면_변경할_수_없다() {
        when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);

        assertThatThrownBy(() -> ownerOrderableShopService.changeOpenStatus(OTHER_OWNER_ID, ORDERABLE_SHOP_ID, true))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ApiResponseCode.FORBIDDEN_SHOP_OWNER);

        assertThat(orderableShop.getShop().getShopOperation().isOpen()).isTrue();
    }

    @Test
    @DisplayName("사장님이 가진 주문 가능 상점을 반환한다")
    void 사장님이_가진_주문_가능_상점을_반환한다() {
        when(orderableShopRepository.findAllByOwnerId(OWNER_ID)).thenReturn(List.of(orderableShop));

        OwnerOrderableShopsResponse response = ownerOrderableShopService.getOrderableShops(OWNER_ID);

        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(response.shops()).singleElement()
            .satisfies(it -> {
                assertThat(it.orderableShopId()).isEqualTo(orderableShop.getId());
                assertThat(it.shopId()).isEqualTo(orderableShop.getShop().getId());
                assertThat(it.name()).isEqualTo("김밥천국");
                assertThat(it.address()).isEqualTo("천안시 동남구 병천면 1600");
                assertThat(it.isOpen()).isTrue();
            });
    }

    @Test
    @DisplayName("주문 가능 상점이 없으면 빈 목록을 반환한다")
    void 주문_가능_상점이_없으면_빈_목록을_반환한다() {
        when(orderableShopRepository.findAllByOwnerId(OWNER_ID)).thenReturn(List.of());

        OwnerOrderableShopsResponse response = ownerOrderableShopService.getOrderableShops(OWNER_ID);

        assertThat(response.totalCount()).isZero();
        assertThat(response.shops()).isEmpty();
    }

    private OrderableShop 영업_종료된_상점() {
        OrderableShop closedShop = OrderableShopFixture.영업시간이_아닌_김밥천국();
        Owner owner = OwnerFixture.성빈_사장님();
        ReflectionTestUtils.setField(owner, "id", OWNER_ID);
        ReflectionTestUtils.setField(closedShop.getShop(), "owner", owner);
        return closedShop;
    }
}
