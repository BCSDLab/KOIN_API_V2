package in.koreatech.koin.unit.domain.address;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.order.address.exception.AddressErrorCode;
import in.koreatech.koin.domain.order.address.exception.AddressException;
import in.koreatech.koin.domain.order.address.model.OffCampusDeliveryAddress;
import in.koreatech.koin.domain.order.address.service.OffCampusAddressValidator;
import in.koreatech.koin.unit.fixutre.AddressFixture;

@ExtendWith(MockitoExtension.class)
public class AddressTest {

    private final OffCampusAddressValidator offCampusAddressValidator = new OffCampusAddressValidator();
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
            assertDoesNotThrow(() -> offCampusAddressValidator.validateAddress(validAddress));
        }

        @Test
        void 교외_배달_불가_지역_주소를_검증시_예외가_발생한다() {
            AddressException exception = assertThrows(AddressException.class, () -> {
                offCampusAddressValidator.validateAddress(invalidAddress);
            });

            assertThat(exception.getErrorCode()).isEqualTo(AddressErrorCode.INVALID_DELIVERY_AREA);
        }
    }
}
