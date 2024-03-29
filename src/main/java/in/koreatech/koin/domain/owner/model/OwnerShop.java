package in.koreatech.koin.domain.owner.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash(value = "owner_shop@", timeToLive = 60 * 60 * 2)
public class OwnerShop {

    @Id
    private Long ownerId;
    private Long shopId;

    @Builder
    private OwnerShop(Long ownerId, Long shopId) {
        this.ownerId = ownerId;
        this.shopId = shopId;
    }
}
