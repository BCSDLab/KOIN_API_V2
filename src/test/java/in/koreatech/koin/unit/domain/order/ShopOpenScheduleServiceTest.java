package in.koreatech.koin.unit.domain.order;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopOpenInfo;
import in.koreatech.koin.domain.order.shop.model.domain.OrderableShopOpenStatus;
import in.koreatech.koin.domain.order.shop.service.ShopOpenScheduleService;
import in.koreatech.koin.unit.fixutre.OrderableShopOpenInfoFixture;

@ExtendWith(MockitoExtension.class)
public class ShopOpenScheduleServiceTest {

    @InjectMocks
    private ShopOpenScheduleService shopOpenScheduleService;

    private OrderableShopOpenInfo 수요일;

    @BeforeEach
    void setUp() {
        수요일 = OrderableShopOpenInfoFixture.영업_정보(1, "WEDNESDAY", false, LocalTime.of(8, 30), LocalTime.of(22, 0));
    }

    @Test
    @DisplayName("사장님이 영업 중 상태로 설정한 상점은 요일별 영업 정보와 현재 요일, 시간과 관련 없이 항상 OPERATING 상태여야 한다")
    void determineOpenStatus_ShopOpenAlwaysOperating() {
        // Given
        Boolean shopIsOpen = true; // 영업 중 상태 = shopIsOpen 이 true

        // When
        OrderableShopOpenStatus orderableShopOpenStatus = shopOpenScheduleService.determineOpenStatus(수요일,
            DayOfWeek.TUESDAY, LocalTime.now(), shopIsOpen);

        //Then
        assertThat(orderableShopOpenStatus).isEqualTo(OrderableShopOpenStatus.OPERATING);
    }

    @Test
    @DisplayName("가게의 영업 시간이지만, 사장님이 영업 중 상태로 설정 하지 않는 가게는 PREPARING 상태여야 한다")
    void determineOpenStatus_ShopWithinHoursNotOpenPreparing() {
        // Given
        Boolean shopIsOpen = false;
        LocalTime now = LocalTime.of(12, 0);

        // When
        OrderableShopOpenStatus orderableShopOpenStatus = shopOpenScheduleService.determineOpenStatus(수요일,
            DayOfWeek.WEDNESDAY, now, shopIsOpen);

        //Then
        assertThat(orderableShopOpenStatus).isEqualTo(OrderableShopOpenStatus.PREPARING);
    }

    @Test
    @DisplayName("가게의 영업 시간이 아니고, 사장님이 영업 중 상태로 설정 하지 않는 가게는 CLOSED 상태여야 한다")
    void determineOpenStatus_ShopOutsideHoursNotOpenClosed() {
        // Given
        Boolean shopIsOpen = false;
        LocalTime now = LocalTime.of(23, 0);

        // When
        OrderableShopOpenStatus orderableShopOpenStatus = shopOpenScheduleService.determineOpenStatus(수요일,
            DayOfWeek.WEDNESDAY, now, shopIsOpen);

        //Then
        assertThat(orderableShopOpenStatus).isEqualTo(OrderableShopOpenStatus.CLOSED);
    }
}
