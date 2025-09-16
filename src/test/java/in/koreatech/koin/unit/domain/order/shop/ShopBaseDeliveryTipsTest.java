package in.koreatech.koin.unit.domain.order.shop;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.order.shop.model.domain.ShopBaseDeliveryTipRange;
import in.koreatech.koin.domain.order.shop.model.domain.ShopBaseDeliveryTips;
import in.koreatech.koin.domain.order.shop.model.entity.delivery.ShopBaseDeliveryTip;

public class ShopBaseDeliveryTipsTest {
    @Nested
    class calculateDeliveryTip {

        @Test
        void 빈_배달비_목록이면_배달비는_0원이다() {
            // given
            ShopBaseDeliveryTips tips = new ShopBaseDeliveryTips();

            // when
            Integer deliveryTip = tips.calculateDeliveryTip(10_000);

            // then
            assertThat(deliveryTip).isEqualTo(0);
        }

        @Test
        void 주문금액_최소값_미만이면_첫_구간_배달비를_적용한다() {
            // given
            ShopBaseDeliveryTip tipA = tip(0, 4000);
            ShopBaseDeliveryTip tipB = tip(15_000, 3000);
            ShopBaseDeliveryTip tipC = tip(30_000, 0);

            ShopBaseDeliveryTips tips = ShopBaseDeliveryTips.of(tipB, tipC, tipA);

            // when
            Integer deliveryTip = tips.calculateDeliveryTip(1_000);

            // then
            assertThat(deliveryTip).isEqualTo(4000);
        }

        @Test
        void 주문금액에_맞는_구간의_배달비를_적용한다() {
            // given
            ShopBaseDeliveryTip tipA = tip(0, 4000);
            ShopBaseDeliveryTip tipB = tip(15_000, 3000);
            ShopBaseDeliveryTip tipC = tip(30_000, 0);

            ShopBaseDeliveryTips tips = ShopBaseDeliveryTips.of(tipC, tipA, tipB);

            // when
            Integer midAmountTip = tips.calculateDeliveryTip(16_000);
            Integer thresholdTip = tips.calculateDeliveryTip(30_000);
            Integer overMaxTip = tips.calculateDeliveryTip(35_000);

            // then
            assertThat(midAmountTip).isEqualTo(3000);
            assertThat(thresholdTip).isEqualTo(0);
            assertThat(overMaxTip).isEqualTo(0);
        }

        @Test
        void 최솟값이_0이_아닐_때_미달하면_첫_구간_배달비를_적용한다() {
            // given
            ShopBaseDeliveryTip tipA = tip(10_000, 4000);
            ShopBaseDeliveryTip tipB = tip(15_000, 3000);
            ShopBaseDeliveryTip tipC = tip(30_000, 0);

            ShopBaseDeliveryTips tips = ShopBaseDeliveryTips.of(tipB, tipC, tipA);

            // when
            Integer deliveryTip = tips.calculateDeliveryTip(5_000);

            // then
            assertThat(deliveryTip).isEqualTo(4000);
        }
    }

    @Nested
    class getDeliveryTipRanges {

        @Test
        void 빈_목록이면_빈_범위를_반환한다() {
            // given
            ShopBaseDeliveryTips tips = ShopBaseDeliveryTips.of();

            // when
            List<ShopBaseDeliveryTipRange> ranges = tips.getDeliveryTipRanges();

            // then
            assertThat(ranges).isEmpty();
        }

        @Test
        void 정렬된_배달비_구간을_생성하고_마지막_toAmount는_null이다() {
            // given
            ShopBaseDeliveryTip tipA = tip(0, 4000);
            ShopBaseDeliveryTip tipB = tip(15_000, 3000);
            ShopBaseDeliveryTip tipC = tip(30_000, 0);

            ShopBaseDeliveryTips tips = ShopBaseDeliveryTips.of(tipB, tipC, tipA);

            // when
            List<ShopBaseDeliveryTipRange> ranges = tips.getDeliveryTipRanges();

            // then
            assertThat(ranges).hasSize(3);

            ShopBaseDeliveryTipRange r0 = ranges.get(0);
            ShopBaseDeliveryTipRange r1 = ranges.get(1);
            ShopBaseDeliveryTipRange r2 = ranges.get(2);

            assertThat(r0.fromAmount()).isEqualTo(0);
            assertThat(r0.toAmount()).isEqualTo(15_000);
            assertThat(r0.fee()).isEqualTo(4000);

            assertThat(r1.fromAmount()).isEqualTo(15_000);
            assertThat(r1.toAmount()).isEqualTo(30_000);
            assertThat(r1.fee()).isEqualTo(3000);

            assertThat(r2.fromAmount()).isEqualTo(30_000);
            assertThat(r2.toAmount()).isNull();
            assertThat(r2.fee()).isEqualTo(0);
        }
    }

    private ShopBaseDeliveryTip tip(Integer orderAmount, Integer fee) {
        try {
            var ctor = ShopBaseDeliveryTip.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            var instance = ctor.newInstance();

            var orderAmountField = ShopBaseDeliveryTip.class.getDeclaredField("orderAmount");
            orderAmountField.setAccessible(true);
            orderAmountField.set(instance, orderAmount);

            var feeField = ShopBaseDeliveryTip.class.getDeclaredField("fee");
            feeField.setAccessible(true);
            feeField.set(instance, fee);

            return instance;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
