package in.koreatech.koin.unit.domain.ShopToOrderable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.domain.shoptoOrderable.dto.ShopToOrderableRequest;
import in.koreatech.koin.domain.shoptoOrderable.model.ShopToOrderable;
import in.koreatech.koin.domain.shoptoOrderable.model.ShopToOrderableDeliveryOption;
import in.koreatech.koin.domain.shoptoOrderable.model.ShopToOrderableRequestStatus;
import in.koreatech.koin.domain.shoptoOrderable.repository.ShopToOrderableRepository;
import in.koreatech.koin.domain.shoptoOrderable.service.ShopToOrderableService;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixture.OwnerFixture;
import in.koreatech.koin.unit.fixture.ShopFixture;

import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ShopToOrderableServiceTest {

    @InjectMocks
    private ShopToOrderableService shopToOrderableService;

    @Mock
    private ShopToOrderableRepository shopToOrderableRepository;

    @Mock
    private ShopRepository shopRepository;

    private Owner owner;
    private User user;
    private Shop shop;
    private ShopToOrderableRequest request;

    @BeforeEach
    void setUp() {
        owner = OwnerFixture.성빈_사장님();
        ReflectionTestUtils.setField(owner, "id", 1);
        shop = ShopFixture.주문전환_이전_상점(owner);

        request = new ShopToOrderableRequest(
            5000,
            true,
            ShopToOrderableDeliveryOption.BOTH,
            1000,
            2000,
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
            when(shopRepository.findById(100)).thenReturn(Optional.of(shop));
            when(shopToOrderableRepository.existsByShopIdAndRequestStatus(100,
                ShopToOrderableRequestStatus.PENDING)).thenReturn(false);
            when(shopToOrderableRepository.existsByShopIdAndRequestStatus(100,
                ShopToOrderableRequestStatus.APPROVED)).thenReturn(false);
            when(shopToOrderableRepository.save(any(ShopToOrderable.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

            // when
            shopToOrderableService.createOrderableRequest(1, request, 100);

            // then
            ArgumentCaptor<ShopToOrderable> captor = ArgumentCaptor.forClass(ShopToOrderable.class);
            verify(shopToOrderableRepository).save(captor.capture());

            ShopToOrderable saved = captor.getValue();
            assertThat(saved.getShop()).isEqualTo(shop);
            assertThat(saved.getMinimumOrderAmount()).isEqualTo(5000);
            assertThat(saved.getIsTakeout()).isTrue();
            assertThat(saved.getDeliveryOption()).isEqualTo(ShopToOrderableDeliveryOption.BOTH);
            assertThat(saved.getCampusDeliveryTip()).isEqualTo(1000);
            assertThat(saved.getOutsideDeliveryTip()).isEqualTo(2000);
            assertThat(saved.getBank()).isEqualTo("국민은행");
            assertThat(saved.getAccountNumber()).isEqualTo("123-456-789");
        }

        @Test
        @DisplayName("존재하지 않는 상점 ID로 신청하면 예외가 발생한다")
        void throwExceptionWhenShopNotFound() {
            // given
            when(shopRepository.findById(999)).thenReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                () -> shopToOrderableService.createOrderableRequest(1, request, 999));

            assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.NOT_FOUND_SHOP);
        }

        @Test
        @DisplayName("이미 신청한 내역이 있으면 예외가 발생한다")
        void throwExceptionWhenAlreadyRequested() {
            // given
            when(shopRepository.findById(100)).thenReturn(Optional.of(shop));
            when(shopToOrderableRepository.existsByShopIdAndRequestStatus(100,
                ShopToOrderableRequestStatus.PENDING)).thenReturn(true);

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                () -> shopToOrderableService.createOrderableRequest(1, request, 100));

            assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.DUPLICATE_REQUESTED_ORDERABLE_SHOP);
        }

        @Test
        @DisplayName("상점 사장님이 아닌 경우 예외가 발생한다")
        void throwExceptionWhenNotShopOwner() {
            // given
            when(shopRepository.findById(100)).thenReturn(Optional.of(shop));

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                () -> shopToOrderableService.createOrderableRequest(2, request, 100));
            assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.FORBIDDEN_SHOP_OWNER);
        }

        @Test
        @DisplayName("이미 승인된 상점이 있으면 예외가 발생한다")
        void throwExceptionWhenAlreadyApproved() {
            // given
            when(shopRepository.findById(100)).thenReturn(Optional.of(shop));
            when(shopToOrderableRepository.existsByShopIdAndRequestStatus(100,
                ShopToOrderableRequestStatus.PENDING)).thenReturn(false);
            when(shopToOrderableRepository.existsByShopIdAndRequestStatus(100,
                ShopToOrderableRequestStatus.APPROVED)).thenReturn(true);

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                () -> shopToOrderableService.createOrderableRequest(1, request, 100));
            assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.DUPLICATE_ORDERABLE_SHOP);
        }
    }
}
