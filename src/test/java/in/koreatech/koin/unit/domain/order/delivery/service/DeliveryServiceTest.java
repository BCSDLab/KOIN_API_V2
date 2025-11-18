package in.koreatech.koin.unit.domain.order.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.order.address.model.CampusDeliveryAddress;
import in.koreatech.koin.domain.order.address.repository.CampusDeliveryAddressRepository;
import in.koreatech.koin.domain.order.delivery.dto.RiderMessageResponse;
import in.koreatech.koin.domain.order.delivery.dto.UserCampusDeliveryAddressRequest;
import in.koreatech.koin.domain.order.delivery.dto.UserDeliveryAddressResponse;
import in.koreatech.koin.domain.order.delivery.dto.UserOffCampusDeliveryAddressRequest;
import in.koreatech.koin.domain.order.delivery.model.OffCampusDeliveryAddress;
import in.koreatech.koin.domain.order.delivery.model.RiderMessage;
import in.koreatech.koin.domain.order.delivery.model.UserDeliveryAddress;
import in.koreatech.koin.domain.order.delivery.repository.RiderMessageRepository;
import in.koreatech.koin.domain.order.delivery.repository.UserDeliveryAddressRepository;
import in.koreatech.koin.domain.order.delivery.service.DeliveryAddressValidator;
import in.koreatech.koin.domain.order.delivery.service.DeliveryService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.unit.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
public class DeliveryServiceTest {

    @InjectMocks
    private DeliveryService deliveryService;

    @Mock
    private DeliveryAddressValidator deliveryAddressValidator;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDeliveryAddressRepository deliveryAddressRepository;
    @Mock
    private CampusDeliveryAddressRepository campusDeliveryAddressRepository;
    @Mock
    private RiderMessageRepository riderMessageRepository;

    @Nested
    class AddOffCampusDeliveryAddressTest {

        @Test
        @DisplayName("교외 배달 주소를 추가하면 주소ID와 전체주소를 반환한다")
        void addOffCampusDeliveryAddress_Success() {
            // given
            Integer userId = 1;
            User user = UserFixture.id_설정_코인_유저(userId);

            UserOffCampusDeliveryAddressRequest request = new UserOffCampusDeliveryAddressRequest(
                "31253", "충청남도", "천안시 동남구", "병천면", "아우내순대길", "병천토박이순대",
                "충청남도 천안시 동남구 병천면 아우내순대길 9", "병천토박이순대"
            );

            String expectedFullAddress = request.address() + " " + request.detailAddress();

            when(userRepository.getById(userId)).thenReturn(user);
            doNothing().when(deliveryAddressValidator).validateOffCampusAddress(any(OffCampusDeliveryAddress.class));
            when(deliveryAddressRepository.save(any(UserDeliveryAddress.class))).thenAnswer(invocation -> {
                UserDeliveryAddress entity = invocation.getArgument(0);
                ReflectionTestUtils.setField(entity, "id", 123);
                return entity;
            });

            // when
            UserDeliveryAddressResponse response = deliveryService.addOffCampusDeliveryAddress(request, userId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.userDeliveryAddressId()).isEqualTo(123);
            assertThat(response.fullAddress()).isEqualTo(expectedFullAddress);

            verify(deliveryAddressValidator).validateOffCampusAddress(argThat(addr ->
                expectedFullAddress.equals(addr.getFullAddress())
            ));
            verify(deliveryAddressRepository).save(any(UserDeliveryAddress.class));
        }
    }

    @Nested
    @DisplayName("교내 배달 주소 테스트")
    class AddCampusDeliveryAddressTest {

        @Test
        @DisplayName("교내 배달 주소를 추가하면 주소ID와 전체주소를 반환한다")
        void addCampusDeliveryAddress_Success() {
            // given
            Integer userId = 1;
            Integer campusAddressId = 42;
            User user = UserFixture.id_설정_코인_유저(userId);

            UserCampusDeliveryAddressRequest request = new UserCampusDeliveryAddressRequest(campusAddressId);

            CampusDeliveryAddress campusDeliveryAddress = mock(CampusDeliveryAddress.class);
            when(campusDeliveryAddress.getFullAddress()).thenReturn("충청남도 천안시 동남구 충절로 1600 한국기술교육대학교");

            when(userRepository.getById(userId)).thenReturn(user);
            when(campusDeliveryAddressRepository.getById(campusAddressId)).thenReturn(campusDeliveryAddress);
            when(deliveryAddressRepository.save(any(UserDeliveryAddress.class))).thenAnswer(invocation -> {
                UserDeliveryAddress entity = invocation.getArgument(0);
                ReflectionTestUtils.setField(entity, "id", 999);
                return entity;
            });

            // when
            UserDeliveryAddressResponse response = deliveryService.addCampusDeliveryAddress(request, userId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.userDeliveryAddressId()).isEqualTo(999);
            assertThat(response.fullAddress()).isEqualTo("충청남도 천안시 동남구 충절로 1600 한국기술교육대학교");

            verify(campusDeliveryAddressRepository).getById(campusAddressId);
            verify(deliveryAddressRepository).save(any(UserDeliveryAddress.class));
        }
    }

    @Nested
    @DisplayName("배달기사 요청사항 조회 테스트")
    class GetRiderMessageTest {

        @Test
        @DisplayName("요청사항 목록을 반환한다")
        void getRiderMessage_ReturnsList() {
            // given
            RiderMessage m1 = mock(RiderMessage.class);
            RiderMessage m2 = mock(RiderMessage.class);
            when(m1.getContent()).thenReturn("문 앞에 놔주세요");
            when(m2.getContent()).thenReturn("도착 시 연락 부탁드립니다");

            when(riderMessageRepository.findAll()).thenReturn(List.of(m1, m2));

            // when
            RiderMessageResponse response = deliveryService.getRiderMessage();

            // then
            assertThat(response).isNotNull();
            assertThat(response.count()).isEqualTo(2);
            assertThat(response.contents()).hasSize(2);
            assertThat(response.contents().get(0).content()).isEqualTo("문 앞에 놔주세요");
            assertThat(response.contents().get(1).content()).isEqualTo("도착 시 연락 부탁드립니다");
        }
    }
}
