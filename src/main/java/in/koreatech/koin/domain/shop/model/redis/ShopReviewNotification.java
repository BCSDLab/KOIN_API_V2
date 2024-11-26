package in.koreatech.koin.domain.shop.model.redis;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ShopReviewNotification {

    private Integer shopId;

    private Integer studentId;

    @Builder
    private ShopReviewNotification(Integer shopId, Integer studentId) {
        this.shopId = shopId;
        this.studentId = studentId;
    }
}
