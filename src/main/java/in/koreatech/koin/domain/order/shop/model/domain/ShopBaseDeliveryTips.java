package in.koreatech.koin.domain.order.shop.model.domain;

import static jakarta.persistence.CascadeType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import in.koreatech.koin.domain.order.shop.model.entity.delivery.ShopBaseDeliveryTip;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
public class ShopBaseDeliveryTips {

    @OneToMany(mappedBy = "shop", orphanRemoval = true, cascade = {PERSIST, REFRESH, MERGE, REMOVE})
    private List<ShopBaseDeliveryTip> baseDeliveryTips = new ArrayList<>();

    public ShopBaseDeliveryTips(List<ShopBaseDeliveryTip> baseDeliveryTips) {
        if (baseDeliveryTips != null) {
            this.baseDeliveryTips.clear();
            this.baseDeliveryTips.addAll(baseDeliveryTips);
        }
    }

    public static ShopBaseDeliveryTips of(List<ShopBaseDeliveryTip> tips) {
        return new ShopBaseDeliveryTips(tips);
    }

    public static ShopBaseDeliveryTips of(ShopBaseDeliveryTip... tips) {
        return new ShopBaseDeliveryTips(tips == null ? List.of() : Arrays.asList(tips));
    }

    public Integer getMinimumDeliveryTip() {
        return baseDeliveryTips.stream()
            .mapToInt(ShopBaseDeliveryTip::getFee)
            .min()
            .orElse(0);
    }

    public Integer getMaximumDeliveryTip() {
        return baseDeliveryTips.stream()
            .mapToInt(ShopBaseDeliveryTip::getFee)
            .max()
            .orElse(0);
    }

    public List<ShopBaseDeliveryTipRange> getDeliveryTipRanges() {
        if (baseDeliveryTips.isEmpty()) {
            return Collections.emptyList();
        }

        List<ShopBaseDeliveryTip> sortedTips = getSortedByOrderAmount();
        List<ShopBaseDeliveryTipRange> ranges = new ArrayList<>();

        for (int i = 0; i < sortedTips.size(); i++) {
            ShopBaseDeliveryTip currentTip = sortedTips.get(i);
            Integer toAmount = null;

            if (i < sortedTips.size() - 1) {
                toAmount = sortedTips.get(i + 1).getOrderAmount();
            }

            ranges.add(new ShopBaseDeliveryTipRange(
                currentTip.getOrderAmount(),
                toAmount,
                currentTip.getFee()
            ));
        }
        return ranges;
    }

    private List<ShopBaseDeliveryTip> getSortedByOrderAmount() {
        return baseDeliveryTips.stream()
            .sorted(Comparator.comparing(ShopBaseDeliveryTip::getOrderAmount))
            .collect(Collectors.toList());
    }

    public Integer calculateDeliveryTip(Integer orderAmount) {
        // 비즈니스 로직 상 비어 있으면 안 되지만 오류로 비어 있는 경우를 대비
        if (baseDeliveryTips.isEmpty()) {
            return 0;
        }

        // 현재 주문 금액이 만족 하는 모든 구간 중 기준 금액(orderAmount)이 가장 높은 구간 찾기
        Optional<ShopBaseDeliveryTip> fitBaseDeliveryTip = baseDeliveryTips.stream()
            .filter(baseDeliveryTip -> orderAmount >= baseDeliveryTip.getOrderAmount())
            .max(Comparator.comparing(ShopBaseDeliveryTip::getOrderAmount));

        if (fitBaseDeliveryTip.isPresent()) {
            return fitBaseDeliveryTip.get().getFee();
        }

        // 최소 주문 금액 미달. 가장 비싼 배달비 적용.
        return getSortedByOrderAmount().get(0).getFee();
    }
}
