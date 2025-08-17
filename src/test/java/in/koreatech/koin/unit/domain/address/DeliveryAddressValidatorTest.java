package in.koreatech.koin.unit.domain.address;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.domain.order.delivery.model.OffCampusDeliveryAddress;
import in.koreatech.koin.domain.order.delivery.service.DeliveryAddressValidator;
import in.koreatech.koin.unit.fixture.AddressFixture;

@ExtendWith(MockitoExtension.class)
public class DeliveryAddressValidatorTest {

    private final DeliveryAddressValidator deliveryAddressValidator = new DeliveryAddressValidator();
    private OffCampusDeliveryAddress validAddress;
    private OffCampusDeliveryAddress invalidAddress;

    @BeforeEach
    void init() {
        validAddress = AddressFixture.교외_배달_가능_지역();
        invalidAddress = AddressFixture.교외_배달_불가_지역();
    }

    @Nested
    class validOffCampusDeliveryAddress {

        @Test
        void 교외_배달_가능_지역_주소를_검증한다() {
            assertDoesNotThrow(() -> deliveryAddressValidator.validateOffCampusAddress(validAddress));
        }

        @Test
        void 교외_배달_불가_지역_주소를_검증시_예외가_발생한다() {
            CustomException exception = assertThrows(CustomException.class, () -> {
                deliveryAddressValidator.validateOffCampusAddress(invalidAddress);
            });
            assertEquals(ApiResponseCode.INVALID_DELIVERY_AREA, exception.getErrorCode());
        }
    }
}
