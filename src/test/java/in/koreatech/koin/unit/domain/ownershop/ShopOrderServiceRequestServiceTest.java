package in.koreatech.koin.unit.domain.ownershop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.ownershop.dto.ShopOrderServiceRequestRequest;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestDeliveryOption;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceTransferData;
import in.koreatech.koin.domain.ownershop.repository.ShopOrderServiceRequestRepository;
import in.koreatech.koin.domain.ownershop.service.ShopOrderServiceRequestService;
import in.koreatech.koin.domain.shop.model.menu.Menu;
import in.koreatech.koin.domain.shop.model.menu.MenuCategory;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixture.OwnerFixture;
import in.koreatech.koin.unit.fixture.ShopFixture;

@ExtendWith(MockitoExtension.class)
class ShopOrderServiceRequestServiceTest {

    @InjectMocks
    private ShopOrderServiceRequestService shopOrderServiceRequestService;

    @Mock
    private ShopOrderServiceRequestRepository shopOrderServiceRequestRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private OrderableShopRepository orderableShopRepository;

    private Owner owner;
    private User user;
    private Shop shop;
    private ShopOrderServiceRequestRequest request;

    @BeforeEach
    void setUp() {
        owner = OwnerFixture.성빈_사장님();
        ReflectionTestUtils.setField(owner, "id", 1);
        shop = ShopFixture.주문전환_이전_상점(owner);

        MenuCategory category = MenuCategory.builder().shop(shop).name("메인 메뉴").build();
        ReflectionTestUtils.setField(category, "id", 1);
        shop.getMenuCategories().add(category);
        Menu menu = Menu.builder().shop(shop).name("짜장면").build();
        ReflectionTestUtils.setField(menu, "id", 1);
        shop.getMenus().add(menu);
        ShopOrderServiceTransferData transferData = new ShopOrderServiceTransferData(
            List.of(),
            List.of(new ShopOrderServiceTransferData.Category(1, "메인 메뉴", List.of(
                new ShopOrderServiceTransferData.Menu(1, "짜장면", null, true, 5000, null, List.of())
            )))
        );

        request = new ShopOrderServiceRequestRequest(
            5000,
            true,
            ShopOrderServiceRequestDeliveryOption.BOTH,
            1000,
            transferData,
            "https://example.com/business_license.jpg",
            "https://example.com/business_certificate.jpg",
            "https://example.com/bank_copy.jpg",
            "국민은행",
            "123-456-789"
        );
    }

    @Nested
    @DisplayName("주문 가능 상점 신청 생성 테스트")
    class CreateOrderableRequestTest {

        @Test
        @DisplayName("정상적으로 주문 가능 상점 신청이 생성된다")
        void createOrderableRequestSuccessfully() {
            // given
            when(shopRepository.getByIdForUpdate(100)).thenReturn(shop);
            when(shopOrderServiceRequestRepository.existsByShopIdAndRequestStatus(100,
                ShopOrderServiceRequestStatus.PENDING)).thenReturn(false);
            when(shopOrderServiceRequestRepository.existsByShopIdAndRequestStatus(100,
                ShopOrderServiceRequestStatus.APPROVED)).thenReturn(false);
            when(shopOrderServiceRequestRepository.save(any(ShopOrderServiceRequest.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

            // when
            shopOrderServiceRequestService.createOrderableRequest(1, request, 100);

            // then
            ArgumentCaptor<ShopOrderServiceRequest> captor = ArgumentCaptor.forClass(ShopOrderServiceRequest.class);
            verify(shopOrderServiceRequestRepository).save(captor.capture());

            ShopOrderServiceRequest saved = captor.getValue();
            assertThat(saved.getShop()).isEqualTo(shop);
            assertThat(saved.getMinimumOrderAmount()).isEqualTo(5000);
            assertThat(saved.getIsTakeout()).isTrue();
            assertThat(saved.getDeliveryOption()).isEqualTo(ShopOrderServiceRequestDeliveryOption.BOTH);
            assertThat(saved.getDeliveryTip()).isEqualTo(1000);
            assertThat(saved.getTransferData()).isEqualTo(request.transferData());
            assertThat(saved.getBank()).isEqualTo("국민은행");
            assertThat(saved.getAccountNumber()).isEqualTo("123-456-789");
        }

        @Test
        @DisplayName("이미 신청한 내역이 있으면 예외가 발생한다")
        void throwExceptionWhenAlreadyRequested() {
            // given
            when(shopRepository.getByIdForUpdate(100)).thenReturn(shop);
            when(shopOrderServiceRequestRepository.existsByShopIdAndRequestStatus(100,
                ShopOrderServiceRequestStatus.PENDING)).thenReturn(true);

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                () -> shopOrderServiceRequestService.createOrderableRequest(1, request, 100));

            assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.DUPLICATE_REQUESTED_ORDERABLE_SHOP);
        }

        @Test
        @DisplayName("상점 사장님이 아닌 경우 예외가 발생한다")
        void throwExceptionWhenNotShopOwner() {
            // given
            when(shopRepository.getByIdForUpdate(100)).thenReturn(shop);

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                () -> shopOrderServiceRequestService.createOrderableRequest(2, request, 100));
            assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.FORBIDDEN_SHOP_OWNER);
        }

        @Test
        @DisplayName("이미 승인된 상점이 있으면 예외가 발생한다")
        void throwExceptionWhenAlreadyApproved() {
            // given
            when(shopRepository.getByIdForUpdate(100)).thenReturn(shop);
            when(shopOrderServiceRequestRepository.existsByShopIdAndRequestStatus(100,
                ShopOrderServiceRequestStatus.PENDING)).thenReturn(false);
            when(shopOrderServiceRequestRepository.existsByShopIdAndRequestStatus(100,
                ShopOrderServiceRequestStatus.APPROVED)).thenReturn(true);

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                () -> shopOrderServiceRequestService.createOrderableRequest(1, request, 100));
            assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.DUPLICATE_ORDERABLE_SHOP);
        }
    }
}
