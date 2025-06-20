package in.koreatech.koin.domain.order.shop.model.domain;

import static jakarta.persistence.CascadeType.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
}
