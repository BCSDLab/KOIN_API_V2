package in.koreatech.koin.domain.shop.model.redis;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ShopNotificationBuffer {

    private Integer shopId;

    private Integer studentId;

    @Builder
    private ShopNotificationBuffer(Integer shopId, Integer studentId) {
        this.shopId = shopId;
        this.studentId = studentId;
    }
}
