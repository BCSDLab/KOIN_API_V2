package in.koreatech.koin.unit.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import in.koreatech.koin.domain.order.address.model.CampusDeliveryAddress;
import in.koreatech.koin.domain.order.delivery.model.AddressType;
import in.koreatech.koin.domain.order.delivery.model.OffCampusDeliveryAddress;
import in.koreatech.koin.domain.order.delivery.model.UserDeliveryAddress;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.global.exception.custom.KoinIllegalStateException;
import java.time.LocalDateTime;

import in.koreatech.koin.unit.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UserDeliveryAddress 단위 테스트")
class UserDeliveryAddressTest {

    private User testUser;
    private CampusDeliveryAddress testCampusAddress;
    private OffCampusDeliveryAddress testOffCampusAddress;

    @BeforeEach
    void setUp() {
        testUser = UserFixture.코인_유저();

        testCampusAddress = mock(CampusDeliveryAddress.class);
        when(testCampusAddress.getFullAddress()).thenReturn("한국기술교육대학교 충청남도 천안시 동남구 충절로 1600 201동 316호");

        testOffCampusAddress = OffCampusDeliveryAddress.builder()
                .zipNumber("31254")
                .siDo("충청남도")
                .siGunGu("천안시 동남구")
                .eupMyeonDong("병천면")
                .detailAddress("이우철한방누룽지삼계탕 아우내병천점")
                .fullAddress("충남 천안시 동남구 병천면 아우내순대길 57-5 1층 이우철한방누룽지삼계탕 아우내병천점")
                .build();
    }

    @Nested
    @DisplayName("getFullDeliveryAddress 메소드는")
    class getFullDeliveryAddress_메소드는 {

        @Test
        @DisplayName("주소 타입이 캠퍼스일 경우 캠퍼스 주소 객체의 getFullAddress를 호출한다")
        void 주소_타입이_캠퍼스일_경우_캠퍼스_주소_객체의_getFullAddress를_호출한다() {
            // given
            UserDeliveryAddress userAddress = UserDeliveryAddress.builder()
                    .addressType(AddressType.CAMPUS)
                    .campusDeliveryAddress(testCampusAddress)
                    .offCampusDeliveryAddress(testOffCampusAddress)
                    .build();

            // when
            String fullAddress = userAddress.getFullDeliveryAddress();

            // then
            assertThat(fullAddress).isNotNull();
            assertThat(fullAddress).contains("201동", "316호");
        }

        @Test
        @DisplayName("주소 타입이 교외일 경우 교외 주소 객체의 getFullAddress를 호출한다")
        void 주소_타입이_교외일_경우_교외_주소_객체의_getFullAddress를_호출한다() {
            // given
            UserDeliveryAddress userAddress = UserDeliveryAddress.builder()
                    .addressType(AddressType.OFF_CAMPUS)
                    .campusDeliveryAddress(testCampusAddress)
                    .offCampusDeliveryAddress(testOffCampusAddress)
                    .build();

            // when
            String fullAddress = userAddress.getFullDeliveryAddress();

            // then
            assertThat(fullAddress).isNotNull();
            assertThat(fullAddress).contains("병천면", "이우철한방누룽지삼계탕 아우내병천점");
        }

        @Test
        @DisplayName("주소 타입이 null이면 KoinIllegalStateException을 발생시킨다")
        void 주소_타입이_null이면_예외를_발생시킨다() {
            // given
            UserDeliveryAddress userAddress = UserDeliveryAddress.builder()
                    .addressType(null)
                    .build();

            // when & then
            assertThrows(KoinIllegalStateException.class, userAddress::getFullDeliveryAddress);
        }

        @Test
        @DisplayName("캠퍼스 타입인데 캠퍼스 주소가 null이면 NullPointerException을 발생시킨다")
        void 캠퍼스_타입인데_캠퍼스_주소가_null이면_NPE가_발생한다() {
            // given
            UserDeliveryAddress userAddress = UserDeliveryAddress.builder()
                    .addressType(AddressType.CAMPUS)
                    .campusDeliveryAddress(null)
                    .offCampusDeliveryAddress(testOffCampusAddress)
                    .build();

            // when & then
            assertThrows(NullPointerException.class, userAddress::getFullDeliveryAddress);
        }

        @Test
        @DisplayName("교외 타입인데 교외 주소가 null이면 NullPointerException을 발생시킨다")
        void 교외_타입인데_교외_주소가_null이면_NPE가_발생한다() {
            // given
            UserDeliveryAddress userAddress = UserDeliveryAddress.builder()
                    .addressType(AddressType.OFF_CAMPUS)
                    .campusDeliveryAddress(testCampusAddress)
                    .offCampusDeliveryAddress(null)
                    .build();

            // when & then
            assertThrows(NullPointerException.class, userAddress::getFullDeliveryAddress);
        }
    }

    @Nested
    @DisplayName("정적 팩토리 메소드는")
    class 정적_팩토리_메소드는 {

        @Test
        @DisplayName("ofCampus는 CAMPUS 타입의 주소 객체를 올바르게 생성한다")
        void ofCampus는_CAMPUS_타입의_주소_객체를_올바르게_생성한다() {
            // when
            UserDeliveryAddress userAddress = UserDeliveryAddress.ofCampus(testUser, testCampusAddress);

            // then
            assertThat(userAddress).as("UserDeliveryAddress 객체가 생성되어야 함").isNotNull();
            assertThat(userAddress.getUser()).as("User 필드가 올바르게 설정되어야 함").isEqualTo(testUser);
            assertThat(userAddress.getAddressType()).as("AddressType이 CAMPUS로 설정되어야 함").isEqualTo(AddressType.CAMPUS);
            assertThat(userAddress.getCampusDeliveryAddress()).as("캠퍼스 주소가 올바르게 설정되어야 함").isEqualTo(testCampusAddress);
            assertThat(userAddress.getOffCampusDeliveryAddress()).as("교외 주소는 null이어야 함").isNull();

            LocalDateTime lastUsedAt = userAddress.getLastUsedAt();
            assertThat(lastUsedAt)
                    .as("lastUsedAt은 정적 팩토리 메소드에서 LocalDateTime.now()로 설정되므로 null이 아니어야 함")
                    .isNotNull();

            assertThat(lastUsedAt).isCloseTo(LocalDateTime.now(),
                    within(10, java.time.temporal.ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("ofOffCampus는 OFF_CAMPUS 타입의 주소 객체를 올바르게 생성한다")
        void ofOffCampus는_OFF_CAMPUS_타입의_주소_객체를_올바르게_생성한다() {
            // when
            UserDeliveryAddress userAddress = UserDeliveryAddress.ofOffCampus(testUser, testOffCampusAddress);

            // then
            assertThat(userAddress).as("UserDeliveryAddress 객체가 생성되어야 함").isNotNull();
            assertThat(userAddress.getUser()).as("User 필드가 올바르게 설정되어야 함").isEqualTo(testUser);
            assertThat(userAddress.getAddressType()).as("AddressType이 OFF_CAMPUS로 설정되어야 함").isEqualTo(AddressType.OFF_CAMPUS);
            assertThat(userAddress.getOffCampusDeliveryAddress()).as("교외 주소가 올바르게 설정되어야 함").isEqualTo(testOffCampusAddress);
            assertThat(userAddress.getCampusDeliveryAddress()).as("캠퍼스 주소는 null이어야 함").isNull();

            LocalDateTime lastUsedAt = userAddress.getLastUsedAt();
            assertThat(lastUsedAt)
                    .as("lastUsedAt은 정적 팩토리 메소드에서 LocalDateTime.now()로 설정되므로 null이 아니어야 함")
                    .isNotNull();

            assertThat(lastUsedAt).isCloseTo(LocalDateTime.now(),
                    within(10, java.time.temporal.ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("ofCampus에 null 유저를 전달하면 null이 저장된다")
        void ofCampus에_null_유저를_전달하면_null이_저장된다() {
            // when
            UserDeliveryAddress userAddress = UserDeliveryAddress.ofCampus(null, testCampusAddress);

            // then
            assertThat(userAddress.getUser()).isNull();
            assertThat(userAddress.getAddressType()).isEqualTo(AddressType.CAMPUS);
            assertThat(userAddress.getCampusDeliveryAddress()).isEqualTo(testCampusAddress);
        }

        @Test
        @DisplayName("ofCampus에 null 캠퍼스 주소를 전달하면 null이 저장된다")
        void ofCampus에_null_캠퍼스_주소를_전달하면_null이_저장된다() {
            // when
            UserDeliveryAddress userAddress = UserDeliveryAddress.ofCampus(testUser, null);

            // then
            assertThat(userAddress.getUser()).isEqualTo(testUser);
            assertThat(userAddress.getAddressType()).isEqualTo(AddressType.CAMPUS);
            assertThat(userAddress.getCampusDeliveryAddress()).isNull();
        }

        @Test
        @DisplayName("ofOffCampus에 null 유저를 전달하면 null이 저장된다")
        void ofOffCampus에_null_유저를_전달하면_null이_저장된다() {
            // when
            UserDeliveryAddress userAddress = UserDeliveryAddress.ofOffCampus(null, testOffCampusAddress);

            // then
            assertThat(userAddress.getUser()).isNull();
            assertThat(userAddress.getAddressType()).isEqualTo(AddressType.OFF_CAMPUS);
            assertThat(userAddress.getOffCampusDeliveryAddress()).isEqualTo(testOffCampusAddress);
        }

        @Test
        @DisplayName("ofOffCampus에 null 교외 주소를 전달하면 null이 저장된다")
        void ofOffCampus에_null_교외_주소를_전달하면_null이_저장된다() {
            // when
            UserDeliveryAddress userAddress = UserDeliveryAddress.ofOffCampus(testUser, null);

            // then
            assertThat(userAddress.getUser()).isEqualTo(testUser);
            assertThat(userAddress.getAddressType()).isEqualTo(AddressType.OFF_CAMPUS);
            assertThat(userAddress.getOffCampusDeliveryAddress()).isNull();
        }
    }

    @Nested
    @DisplayName("Builder를 통한 객체 생성 시")
    class Builder를_통한_객체_생성_시 {

        @Test
        @DisplayName("모든 필드가 올바르게 설정된다")
        void 모든_필드가_올바르게_설정된다() {
            // given
            LocalDateTime testTime = LocalDateTime.of(2025, 9, 6, 2, 10, 0);

            // when
            UserDeliveryAddress userAddress = UserDeliveryAddress.builder()
                    .user(testUser)
                    .addressType(AddressType.CAMPUS)
                    .campusDeliveryAddress(testCampusAddress)
                    .offCampusDeliveryAddress(testOffCampusAddress)
                    .lastUsedAt(testTime)
                    .usageCount(5)
                    .isDefault(true)
                    .build();

            // then
            assertThat(userAddress.getUser()).isEqualTo(testUser);
            assertThat(userAddress.getAddressType()).isEqualTo(AddressType.CAMPUS);
            assertThat(userAddress.getCampusDeliveryAddress()).isEqualTo(testCampusAddress);
            assertThat(userAddress.getOffCampusDeliveryAddress()).isEqualTo(testOffCampusAddress);
            assertThat(userAddress.getLastUsedAt()).isEqualTo(testTime);
            assertThat(userAddress.getUsageCount()).isEqualTo(5);
            assertThat(userAddress.getIsDefault()).isTrue();
        }

        @Test
        @DisplayName("필수 필드만 설정하고 나머지는 기본값이 적용된다")
        void 필수_필드만_설정하고_나머지는_기본값이_적용된다() {
            // given & when
            UserDeliveryAddress userAddress = UserDeliveryAddress.builder()
                    .user(testUser)
                    .addressType(AddressType.CAMPUS)
                    .campusDeliveryAddress(testCampusAddress)
                    .build();

            // then
            assertThat(userAddress.getUser()).isEqualTo(testUser);
            assertThat(userAddress.getAddressType()).isEqualTo(AddressType.CAMPUS);
            assertThat(userAddress.getCampusDeliveryAddress()).isEqualTo(testCampusAddress);
            assertThat(userAddress.getOffCampusDeliveryAddress()).isNull();
            assertThat(userAddress.getLastUsedAt()).isNull();
            assertThat(userAddress.getUsageCount()).isNull();
            assertThat(userAddress.getIsDefault()).isNull();
        }
    }

    @Nested
    @DisplayName("실제 주소 데이터 검증")
    class 실제_주소_데이터_검증 {

        @Test
        @DisplayName("한국기술교육대학교 캠퍼스 주소가 올바르게 반환된다")
        void 한국기술교육대학교_캠퍼스_주소가_올바르게_반환된다() {
            // given
            UserDeliveryAddress userAddress = UserDeliveryAddress.ofCampus(testUser, testCampusAddress);

            // when
            String fullAddress = userAddress.getFullDeliveryAddress();

            // then
            assertThat(fullAddress).contains("201동");
            assertThat(fullAddress).contains("316호");
            assertThat(fullAddress).contains("한국기술교육대학교");
            assertThat(fullAddress).contains("충절로 1600");
        }

        @Test
        @DisplayName("병천면 맛집 주소가 올바르게 반환된다")
        void 병천면_맛집_주소가_올바르게_반환된다() {
            // given
            UserDeliveryAddress userAddress = UserDeliveryAddress.ofOffCampus(testUser, testOffCampusAddress);

            // when
            String fullAddress = userAddress.getFullDeliveryAddress();

            // then
            assertThat(fullAddress).contains("천안시");
            assertThat(fullAddress).contains("병천면");
            assertThat(fullAddress).contains("이우철한방누룽지삼계탕 아우내병천점");
        }
    }
}
