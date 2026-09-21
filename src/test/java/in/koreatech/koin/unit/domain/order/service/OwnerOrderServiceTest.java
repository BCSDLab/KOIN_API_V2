package in.koreatech.koin.unit.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.order.order.dto.request.OwnerOrderStatusCriteria;
import in.koreatech.koin.domain.order.order.dto.response.OwnerOrderCountsResponse;
import in.koreatech.koin.domain.order.order.dto.response.OwnerOrderResponse;
import in.koreatech.koin.domain.order.order.dto.response.OwnerOrdersResponse;
import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.model.OrderMenu;
import in.koreatech.koin.domain.order.order.model.OrderStatus;
import in.koreatech.koin.domain.order.order.repository.OrderRepository;
import in.koreatech.koin.domain.order.order.service.OwnerOrderService;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.payment.model.entity.Payment;
import in.koreatech.koin.domain.payment.model.entity.PaymentMethod;
import in.koreatech.koin.domain.payment.repository.PaymentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixture.OrderFixture;
import in.koreatech.koin.unit.fixture.OrderableShopFixture;
import in.koreatech.koin.unit.fixture.OwnerFixture;
import in.koreatech.koin.unit.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class OwnerOrderServiceTest {

    private static final Integer OWNER_ID = 100;
    private static final Integer OTHER_OWNER_ID = 999;
    private static final Integer ORDERABLE_SHOP_ID = 1;

    @InjectMocks
    private OwnerOrderService ownerOrderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderableShopRepository orderableShopRepository;

    @Mock
    private PaymentRepository paymentRepository;

    private OrderableShop orderableShop;
    private User customer;

    @BeforeEach
    void setUp() {
        orderableShop = OrderableShopFixture.김밥천국(1);

        Owner owner = OwnerFixture.성빈_사장님();
        ReflectionTestUtils.setField(owner, "id", OWNER_ID);
        ReflectionTestUtils.setField(orderableShop.getShop(), "owner", owner);

        customer = UserFixture.코인_유저();
        ReflectionTestUtils.setField(customer, "id", 7);
    }

    @Nested
    @DisplayName("주문 목록 조회")
    class GetOrders {

        @Test
        @DisplayName("신규 탭은 주문 확인중 상태의 주문만 조회한다")
        void 신규_탭은_주문_확인중_상태만_조회한다() {
            Order order = OrderFixture.배달_주문(1, "A1B2C3D4E5", OrderStatus.CONFIRMING, orderableShop, customer);
            when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);
            when(orderRepository.findAllByOrderableShopIdAndStatuses(
                ORDERABLE_SHOP_ID, List.of(OrderStatus.CONFIRMING))).thenReturn(List.of(order));

            OwnerOrdersResponse response = ownerOrderService.getOrders(
                OWNER_ID, ORDERABLE_SHOP_ID, OwnerOrderStatusCriteria.NEW);

            assertThat(response.totalCount()).isEqualTo(1);
            assertThat(response.orders()).singleElement()
                .satisfies(it -> {
                    assertThat(it.id()).isEqualTo(1);
                    assertThat(it.orderNumber()).isEqualTo("A1B2C3D4E5");
                    assertThat(it.orderStatus()).isEqualTo(OrderStatus.CONFIRMING.name());
                    assertThat(it.totalPrice()).isEqualTo(35000);
                    assertThat(it.estimatedArrivalAt()).isNull();
                });
        }

        @Test
        @DisplayName("조리가 시작된 주문은 도착 예정 일시를 함께 반환한다")
        void 조리가_시작된_주문은_도착_예정_일시를_반환한다() {
            Order order = OrderFixture.배달_주문(1, "A1B2C3D4E5", OrderStatus.CONFIRMING, orderableShop, customer);
            order.getOrderDelivery().cooking();

            when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);
            when(orderRepository.findAllByOrderableShopIdAndStatuses(
                ORDERABLE_SHOP_ID, List.of(OrderStatus.COOKING))).thenReturn(List.of(order));

            OwnerOrdersResponse response = ownerOrderService.getOrders(
                OWNER_ID, ORDERABLE_SHOP_ID, OwnerOrderStatusCriteria.COOKING);

            assertThat(response.orders()).singleElement()
                .satisfies(it -> {
                    assertThat(it.orderStatus()).isEqualTo(OrderStatus.COOKING.name());
                    assertThat(it.estimatedArrivalAt()).isNotNull();
                });
        }

        @Test
        @DisplayName("완료 탭은 배달 완료와 반려 상태를 함께 조회한다")
        void 완료_탭은_배달_완료와_반려를_함께_조회한다() {
            when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);
            when(orderRepository.findAllByOrderableShopIdAndStatuses(
                ORDERABLE_SHOP_ID, List.of(OrderStatus.DELIVERED, OrderStatus.CANCELED)))
                .thenReturn(List.of());

            ownerOrderService.getOrders(OWNER_ID, ORDERABLE_SHOP_ID, OwnerOrderStatusCriteria.COMPLETED);

            verify(orderRepository).findAllByOrderableShopIdAndStatuses(
                ORDERABLE_SHOP_ID, List.of(OrderStatus.DELIVERED, OrderStatus.CANCELED));
        }

        @Test
        @DisplayName("주문이 없으면 빈 목록을 반환한다")
        void 주문이_없으면_빈_목록을_반환한다() {
            when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);
            when(orderRepository.findAllByOrderableShopIdAndStatuses(any(), any())).thenReturn(List.of());

            OwnerOrdersResponse response = ownerOrderService.getOrders(
                OWNER_ID, ORDERABLE_SHOP_ID, OwnerOrderStatusCriteria.NEW);

            assertThat(response.totalCount()).isZero();
            assertThat(response.orders()).isEmpty();
        }

        @Test
        @DisplayName("상점의 사장님이 아니면 조회할 수 없다")
        void 상점의_사장님이_아니면_조회할_수_없다() {
            when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);

            assertThatThrownBy(() -> ownerOrderService.getOrders(
                OTHER_OWNER_ID, ORDERABLE_SHOP_ID, OwnerOrderStatusCriteria.NEW))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ApiResponseCode.FORBIDDEN_SHOP_OWNER);

            verify(orderRepository, never()).findAllByOrderableShopIdAndStatuses(any(), any());
        }
    }

    @Nested
    @DisplayName("상태별 주문 수 조회")
    class GetOrderCounts {

        @Test
        @DisplayName("탭별로 해당 상태의 주문 수를 센다")
        void 탭별로_해당_상태의_주문_수를_센다() {
            when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);
            when(orderRepository.countByOrderableShopIdAndStatusIn(
                ORDERABLE_SHOP_ID, OwnerOrderStatusCriteria.NEW.getOrderStatuses())).thenReturn(3L);
            when(orderRepository.countByOrderableShopIdAndStatusIn(
                ORDERABLE_SHOP_ID, OwnerOrderStatusCriteria.COOKING.getOrderStatuses())).thenReturn(2L);
            when(orderRepository.countByOrderableShopIdAndStatusIn(
                ORDERABLE_SHOP_ID, OwnerOrderStatusCriteria.DELIVERING.getOrderStatuses())).thenReturn(1L);
            when(orderRepository.countByOrderableShopIdAndStatusIn(
                ORDERABLE_SHOP_ID, OwnerOrderStatusCriteria.COMPLETED.getOrderStatuses())).thenReturn(14L);

            OwnerOrderCountsResponse response = ownerOrderService.getOrderCounts(OWNER_ID, ORDERABLE_SHOP_ID);

            assertThat(response.newCount()).isEqualTo(3L);
            assertThat(response.cookingCount()).isEqualTo(2L);
            assertThat(response.deliveringCount()).isEqualTo(1L);
            assertThat(response.completedCount()).isEqualTo(14L);
        }

        @Test
        @DisplayName("완료 수는 배달 완료와 반려를 함께 센다")
        void 완료_수는_배달_완료와_반려를_함께_센다() {
            when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);
            when(orderRepository.countByOrderableShopIdAndStatusIn(eq(ORDERABLE_SHOP_ID), any())).thenReturn(0L);

            ownerOrderService.getOrderCounts(OWNER_ID, ORDERABLE_SHOP_ID);

            verify(orderRepository).countByOrderableShopIdAndStatusIn(
                ORDERABLE_SHOP_ID, List.of(OrderStatus.DELIVERED, OrderStatus.CANCELED));
        }

        @Test
        @DisplayName("주문이 없으면 모든 탭이 0이다")
        void 주문이_없으면_모든_탭이_0이다() {
            when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);
            when(orderRepository.countByOrderableShopIdAndStatusIn(eq(ORDERABLE_SHOP_ID), any())).thenReturn(0L);

            OwnerOrderCountsResponse response = ownerOrderService.getOrderCounts(OWNER_ID, ORDERABLE_SHOP_ID);

            assertThat(response.newCount()).isZero();
            assertThat(response.cookingCount()).isZero();
            assertThat(response.deliveringCount()).isZero();
            assertThat(response.completedCount()).isZero();
        }

        @Test
        @DisplayName("상점의 사장님이 아니면 조회할 수 없다")
        void 상점의_사장님이_아니면_조회할_수_없다() {
            when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);

            assertThatThrownBy(() -> ownerOrderService.getOrderCounts(OTHER_OWNER_ID, ORDERABLE_SHOP_ID))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ApiResponseCode.FORBIDDEN_SHOP_OWNER);

            verify(orderRepository, never()).countByOrderableShopIdAndStatusIn(any(), any());
        }
    }

    @Nested
    @DisplayName("주문 상세 조회")
    class GetOrder {

        @Test
        @DisplayName("주문 상품, 받는 사람, 결제 정보를 함께 반환한다")
        void 주문_상품과_받는_사람과_결제_정보를_반환한다() {
            Order order = OrderFixture.배달_주문(1, "A1B2C3D4E5", OrderStatus.CONFIRMING, orderableShop, customer);
            OrderMenu orderMenu = OrderFixture.짜장면(order);
            order.addOrderMenu(orderMenu);

            when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);
            when(orderRepository.findByIdAndOrderableShopId(1, ORDERABLE_SHOP_ID)).thenReturn(Optional.of(order));
            when(paymentRepository.getByOrderId(1)).thenReturn(payment());

            OwnerOrderResponse response = ownerOrderService.getOrder(OWNER_ID, ORDERABLE_SHOP_ID, 1);

            assertThat(response.orderNumber()).isEqualTo("A1B2C3D4E5");
            assertThat(response.orderedAt()).isEqualTo(LocalDateTime.of(2026, 9, 20, 18, 42));

            assertThat(response.orderMenus()).singleElement()
                .satisfies(menu -> {
                    assertThat(menu.menuName()).isEqualTo("짜장면");
                    assertThat(menu.menuPriceName()).isEqualTo("곱빼기");
                    assertThat(menu.menuPrice()).isEqualTo(14000);
                    assertThat(menu.quantity()).isEqualTo(2);
                    assertThat(menu.options()).singleElement()
                        .satisfies(option -> {
                            assertThat(option.optionGroupName()).isEqualTo("추가 선택");
                            assertThat(option.optionName()).isEqualTo("단무지 추가");
                            assertThat(option.optionPrice()).isEqualTo(500);
                        });
                });

            assertThat(response.receiver().name()).isEqualTo(customer.getName());
            assertThat(response.receiver().phoneNumber()).isEqualTo("01012341234");
            assertThat(response.receiver().address()).isEqualTo("충청남도 천안시 동남구 병천면 충절로 1600");
            assertThat(response.receiver().addressDetail()).isEqualTo("2공학관 201호");
            assertThat(response.receiver().toOwner()).isEqualTo("문 앞에 두고 벨 눌러주세요.");
            assertThat(response.receiver().provideCutlery()).isTrue();

            assertThat(response.payment().method()).isEqualTo(PaymentMethod.CARD.name());
            assertThat(response.payment().totalProductPrice()).isEqualTo(32000);
            assertThat(response.payment().deliveryTip()).isEqualTo(3000);
            assertThat(response.payment().totalPrice()).isEqualTo(35000);
        }

        @Test
        @DisplayName("반려된 주문은 반려 일시와 사유를 반환한다")
        void 반려된_주문은_반려_일시와_사유를_반환한다() {
            Order order = OrderFixture.배달_주문(1, "A1B2C3D4E5", OrderStatus.CONFIRMING, orderableShop, customer);
            order.cancel("재료 소진");

            when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);
            when(orderRepository.findByIdAndOrderableShopId(1, ORDERABLE_SHOP_ID)).thenReturn(Optional.of(order));
            when(paymentRepository.getByOrderId(1)).thenReturn(payment());

            OwnerOrderResponse response = ownerOrderService.getOrder(OWNER_ID, ORDERABLE_SHOP_ID, 1);

            assertThat(response.orderStatus()).isEqualTo(OrderStatus.CANCELED.name());
            assertThat(response.canceledReason()).isEqualTo("재료 소진");
            assertThat(response.canceledAt()).isNotNull();
            assertThat(response.deliveredAt()).isNull();
        }

        @Test
        @DisplayName("상점에 속한 주문이 없으면 조회할 수 없다")
        void 상점에_속한_주문이_없으면_조회할_수_없다() {
            when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);
            when(orderRepository.findByIdAndOrderableShopId(1, ORDERABLE_SHOP_ID))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ownerOrderService.getOrder(OWNER_ID, ORDERABLE_SHOP_ID, 1))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ApiResponseCode.NOT_FOUND_ORDER);

            verify(paymentRepository, never()).getByOrderId(any());
        }

        @Test
        @DisplayName("상점 식별자로 범위를 좁혀 조회한다")
        void 상점_식별자로_범위를_좁혀_조회한다() {
            Order order = OrderFixture.배달_주문(1, "A1B2C3D4E5", OrderStatus.CONFIRMING, orderableShop, customer);

            when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);
            when(orderRepository.findByIdAndOrderableShopId(1, ORDERABLE_SHOP_ID))
                .thenReturn(Optional.of(order));
            when(paymentRepository.getByOrderId(1)).thenReturn(payment());

            ownerOrderService.getOrder(OWNER_ID, ORDERABLE_SHOP_ID, 1);

            verify(orderRepository).findByIdAndOrderableShopId(1, ORDERABLE_SHOP_ID);
        }

        @Test
        @DisplayName("상점의 사장님이 아니면 조회할 수 없다")
        void 상점의_사장님이_아니면_조회할_수_없다() {
            when(orderableShopRepository.getById(ORDERABLE_SHOP_ID)).thenReturn(orderableShop);

            assertThatThrownBy(() -> ownerOrderService.getOrder(OTHER_OWNER_ID, ORDERABLE_SHOP_ID, 1))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ApiResponseCode.FORBIDDEN_SHOP_OWNER);

            verify(orderRepository, never()).findByIdAndOrderableShopId(any(), any());
        }

        private Payment payment() {
            return Payment.builder()
                .paymentKey("payment-key")
                .amount(35000)
                .paymentStatus(in.koreatech.koin.domain.payment.model.entity.PaymentStatus.DONE)
                .paymentMethod(PaymentMethod.CARD)
                .description("짜장면 외 1건")
                .requestedAt(LocalDateTime.of(2026, 9, 20, 18, 42))
                .approvedAt(LocalDateTime.of(2026, 9, 20, 18, 42))
                .receipt("https://receipt.test")
                .isDeleted(false)
                .build();
        }
    }
}
