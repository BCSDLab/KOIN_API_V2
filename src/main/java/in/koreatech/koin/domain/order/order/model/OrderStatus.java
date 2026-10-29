package in.koreatech.koin.domain.order.order.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    CONFIRMING("주문 확인중", true),
    COOKING("조리 중", true),
    PACKAGED("포장 완료", true),
    PICKED_UP("포장 수령", false),
    DELIVERING("배달 중", true),
    DELIVERED("배달 완료", false),
    CANCELED("취소", false),
    ;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_NEXT_STATUSES = Map.of(
        CONFIRMING, Set.of(COOKING, CANCELED),
        COOKING, Set.of(DELIVERING, PACKAGED),
        PACKAGED, Set.of(PICKED_UP),
        DELIVERING, Set.of(DELIVERED)
    );

    private final String description;
    private final boolean inProgress;

    public static List<OrderStatus> inProgressStatuses() {
        return Arrays.stream(values())
            .filter(OrderStatus::isInProgress)
            .toList();
    }

    public boolean canChangeTo(OrderStatus nextStatus) {
        return ALLOWED_NEXT_STATUSES.getOrDefault(this, Set.of()).contains(nextStatus);
    }
}
