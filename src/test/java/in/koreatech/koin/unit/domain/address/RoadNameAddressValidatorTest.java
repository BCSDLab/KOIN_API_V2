package in.koreatech.koin.unit.domain.address;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.address.client.AddressClient;
import in.koreatech.koin.domain.address.service.RoadNameAddressValidator;
import in.koreatech.koin.domain.order.address.dto.RoadNameAddressApiResponse;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixture.AddressFixture;

@ExtendWith(MockitoExtension.class)
public class RoadNameAddressValidatorTest {

    @InjectMocks
    private RoadNameAddressValidator roadNameAddressValidator;

    @Mock
    private AddressClient addressClient;

    private static final String successAddress = "충청남도 천안시 동남구 병천면 충절로 1600";
    private static final String failAddress = "존재하지않는주소";
    private RoadNameAddressApiResponse successResponse;
    private RoadNameAddressApiResponse failResponse;

    @Nested
    @DisplayName("도로명 주소 검증 성공")
    class ValidateSuccess {

        @BeforeEach
        void setUpRoadNameAddressApiResponse() {
            successResponse = AddressFixture.주소_검색_결과_한국기술교육대학교();
        }

        @Test
        void 정상적인_주소를_입력하여_검증을_통과한다() {
            when(addressClient.searchAddress(successAddress, 1, 1)).thenReturn(successResponse);

            assertThatCode(() -> {
                roadNameAddressValidator.validateAddress(successAddress);
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("도로명 주소 검증 실패")
    class ValidateFail {

        @BeforeEach
        void setUpRoadNameAddressApiResponse() {
            failResponse = AddressFixture.주소_검색_결과_없음();
        }

        @Test
        void 비정상적인_주소를_입력하여_예외가_발생한다() {
            when(addressClient.searchAddress(failAddress, 1, 1)).thenReturn(failResponse);

            assertThatThrownBy(() -> {
                roadNameAddressValidator.validateAddress(failAddress);
            }).isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApiResponseCode.INVALID_ADDRESS_FORMAT);
        }

        @Test
        void 외부_API_호출_오류가_발생하여_검증이_생략된다() {
            when(addressClient.searchAddress(successAddress, 1, 1))
                .thenThrow(CustomException.of(ApiResponseCode.EXTERNAL_API_ERROR));

            assertThatCode(() -> {
                roadNameAddressValidator.validateAddress(successAddress);
            }).doesNotThrowAnyException();
        }
    }
}
