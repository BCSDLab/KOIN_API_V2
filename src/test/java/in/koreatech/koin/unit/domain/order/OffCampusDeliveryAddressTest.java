package in.koreatech.koin.unit.domain.order;

import in.koreatech.koin.domain.order.delivery.model.OffCampusDeliveryAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("OffCampusDeliveryAddress 단위 테스트")
public class OffCampusDeliveryAddressTest {

    private OffCampusDeliveryAddress deliveryAddress;

    @BeforeEach
    void setUp() {
        deliveryAddress = OffCampusDeliveryAddress.builder()
                .zipNumber("31253")
                .siDo("충청남도")
                .siGunGu("천안시 동남구")
                .eupMyeonDong("병천면")
                .road("충절로")
                .building("한국기술교육대학교")
                .address("충청남도 천안시 동남구 병천면 충절로 1600")
                .detailAddress("한국기술교육대학교")
                .fullAddress("충청남도 천안시 동남구 병천면 충절로 1600 한국기술교육대학교")
                .build();
    }

    @Nested
    @DisplayName("배달 불가능 지역 테스트")
    class InvalidDeliveryAreaTest {

        @Test
        void 시도가_다르면_false를_반환한다() {
            // when
            Boolean result = deliveryAddress.isValidDeliveryArea("충청북도", "천안시 동남구", "병천면");

            // then
            assertThat(result).isFalse();
        }

        @Test
        void 시군구가_다르면_false를_반환한다() {
            // when
            Boolean result = deliveryAddress.isValidDeliveryArea("충청남도", "청주시 상당구", "병천면");

            // then
            assertThat(result).isFalse();
        }

        @Test
        void 읍면동이_다르면_false를_반환한다() {
            // when
            Boolean result = deliveryAddress.isValidDeliveryArea("충청남도", "천안시 동남구", "용암동");

            // then
            assertThat(result).isFalse();
        }

        @Test
        void 모든_지역정보가_다르면_false를_반환한다() {
            // when
            Boolean result = deliveryAddress.isValidDeliveryArea("충청북도", "청주시 상당구", "용암동");

            // then
            assertThat(result).isFalse();
        }

        @Test
        void 시도만_일치하면_false를_반환한다() {
            // when
            Boolean result = deliveryAddress.isValidDeliveryArea("충청남도", "청주시 상당구", "용암동");

            // then
            assertThat(result).isFalse();
        }

        @Test
        void 시도와_시군구만_일치하면_false를_반환한다() {
            // when
            Boolean result = deliveryAddress.isValidDeliveryArea("충청남도", "천안시 동남구", "용암동");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("배달 불가 건물 테스트")
    class IsNotAllowedBuildingTest {

        @Test
        void 건물명이_일치하면_true를_반환한다() {
            // when
            Boolean result = deliveryAddress.isNotAllowedBuilding("한국기술교육대학교");

            // then
            assertThat(result).isTrue();
        }

        @Test
        void 건물명이_다르면_false를_반환한다() {
            // when
            Boolean result = deliveryAddress.isNotAllowedBuilding("다른건물");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class EdgeCaseTest {

        @Test
        void 건물명이_null인_주소와_비교하면_false를_반환한다() {
            // given
            OffCampusDeliveryAddress addressWithNullBuilding = OffCampusDeliveryAddress.builder()
                    .zipNumber("31253")
                    .siDo("충청남도")
                    .siGunGu("천안시 동남구")
                    .eupMyeonDong("병천면")
                    .road("충절로")
                    .building(null)
                    .address("충청남도 천안시 동남구 병천면 충절로 1600")
                    .detailAddress("한국기술교육대학교")
                    .fullAddress("충청남도 천안시 동남구 병천면 충절로 1600 한국기술교육대학교")
                    .build();

            // when
            Boolean result = addressWithNullBuilding.isNotAllowedBuilding("한국기술교육대학교");

            // then
            assertThat(result).isFalse();
        }

        @Test
        void 빈_문자열_건물명과_비교하면_해당하는_결과를_반환한다() {
            // given
            OffCampusDeliveryAddress addressWithEmptyBuilding = OffCampusDeliveryAddress.builder()
                    .zipNumber("31253")
                    .siDo("충청남도")
                    .siGunGu("천안시 동남구")
                    .eupMyeonDong("병천면")
                    .road("충절로")
                    .building("")
                    .address("충청남도 천안시 동남구 병천면 충절로 1600")
                    .detailAddress("한국기술교육대학교")
                    .fullAddress("충청남도 천안시 동남구 병천면 충절로 1600 한국기술교육대학교")
                    .build();

            // when
            Boolean result = addressWithEmptyBuilding.isNotAllowedBuilding("");

            // then
            assertThat(result).isTrue();
        }

        @Test
        void null_값으로_건물명을_비교하면_NullPointerException이_발생한다() {
            // when & then
            assertThrows(NullPointerException.class, () ->
                    deliveryAddress.isNotAllowedBuilding(null));
        }

        @Test
        void 영문_대소문자가_다른_건물명_테스트() {
            // given
            OffCampusDeliveryAddress addressWithEnglishBuilding = OffCampusDeliveryAddress.builder()
                    .zipNumber("31253")
                    .siDo("충청남도")
                    .siGunGu("천안시 동남구")
                    .eupMyeonDong("병천면")
                    .road("충절로")
                    .building("KoreaTech")
                    .address("충청남도 천안시 동남구 병천면 충절로 1600")
                    .detailAddress("KoreaTech")
                    .fullAddress("충청남도 천안시 동남구 병천면 충절로 1600 KoreaTech")
                    .build();

            // when
            Boolean result = addressWithEnglishBuilding.isNotAllowedBuilding("KOREATECH");

            // then
            assertThat(result).isFalse();
        }

        @Test
        void 공백이_포함된_건물명_비교_테스트() {
            // when
            Boolean result = deliveryAddress.isNotAllowedBuilding(" 한국기술교육대학교 ");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("배달 가능 지역 검증 테스트")
    class IsValidDeliveryAddress {

        @Test
        void 시도_시군구_읍면동이_일치하면_true를_반환한다() {
            // when
            Boolean result = deliveryAddress.isValidDeliveryArea("충청남도", "천안시 동남구", "병천면");

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("null 매개변수 테스트")
    class NullParameterTest {

        @Test
        void 시도가_null이면_NPE가_발생한다() {
            // when & then
            assertThrows(NullPointerException.class, () ->
                    deliveryAddress.isValidDeliveryArea(null, "천안시 동남구", "병천면"));
        }

        @Test
        void 시군구가_null이면_NPE가_발생한다() {
            // when & then
            assertThrows(NullPointerException.class, () ->
                    deliveryAddress.isValidDeliveryArea("충청남도", null, "병천면"));
        }

        @Test
        void 읍면동이_null이면_NPE가_발생한다() {
            // when & then
            assertThrows(NullPointerException.class, () ->
                    deliveryAddress.isValidDeliveryArea("충청남도", "천안시 동남구", null));
        }

        @Test
        void 모든_매개변수가_null이면_NPE가_발생한다() {
            // when & then
            assertThrows(NullPointerException.class, () ->
                    deliveryAddress.isValidDeliveryArea(null, null, null));
        }
    }

    @Nested
    @DisplayName("인스턴스 필드가 null인 경우 테스트")
    class InstanceFieldNullTest {

        @Test
        void 인스턴스의_시도가_null인_경우_false를_반환한다() {
            // given
            OffCampusDeliveryAddress addressWithNullSiDo = OffCampusDeliveryAddress.builder()
                    .zipNumber("31253")
                    .siDo(null)
                    .siGunGu("천안시 동남구")
                    .eupMyeonDong("병천면")
                    .road("충절로")
                    .building("한국기술교육대학교")
                    .address("충청남도 천안시 동남구 병천면 충절로 1600")
                    .detailAddress("한국기술교육대학교")
                    .fullAddress("충청남도 천안시 동남구 병천면 충절로 1600 한국기술교육대학교")
                    .build();

            // when
            Boolean result = addressWithNullSiDo.isValidDeliveryArea("충청남도", "천안시 동남구", "병천면");

            // then
            assertThat(result).isFalse();
        }

        @Test
        void 인스턴스의_시군구가_null인_경우_false를_반환한다() {
            // given
            OffCampusDeliveryAddress addressWithNullSiGunGu = OffCampusDeliveryAddress.builder()
                    .zipNumber("31253")
                    .siDo("충청남도")
                    .siGunGu(null)
                    .eupMyeonDong("병천면")
                    .road("충절로")
                    .building("한국기술교육대학교")
                    .address("충청남도 천안시 동남구 병천면 충절로 1600")
                    .detailAddress("한국기술교육대학교")
                    .fullAddress("충청남도 천안시 동남구 병천면 충절로 1600 한국기술교육대학교")
                    .build();

            // when
            Boolean result = addressWithNullSiGunGu.isValidDeliveryArea("충청남도", "천안시 동남구", "병천면");

            // then
            assertThat(result).isFalse();
        }

        @Test
        void 인스턴스의_읍면동이_null인_경우_false를_반환한다() {
            // given
            OffCampusDeliveryAddress addressWithNullEupMyeonDong = OffCampusDeliveryAddress.builder()
                    .zipNumber("31253")
                    .siDo("충청남도")
                    .siGunGu("천안시 동남구")
                    .eupMyeonDong(null)
                    .road("충절로")
                    .building("한국기술교육대학교")
                    .address("충청남도 천안시 동남구 병천면 충절로 1600")
                    .detailAddress("한국기술교육대학교")
                    .fullAddress("충청남도 천안시 동남구 병천면 충절로 1600 한국기술교육대학교")
                    .build();

            // when
            Boolean result = addressWithNullEupMyeonDong.isValidDeliveryArea("충청남도", "천안시 동남구", "병천면");

            // then
            assertThat(result).isFalse();
        }
    }
}
