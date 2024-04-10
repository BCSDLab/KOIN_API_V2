package in.koreatech.koin.domain.owner.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash(value = "owner_shop@", timeToLive = 60 * 60 * 2)
public class OwnerShop {

    @Id
    private Integer ownerId;
    private Integer shopId;

    @Builder
    private OwnerShop(Integer ownerId, Integer shopId) {
        this.ownerId = ownerId;
        this.shopId = shopId;
    }
}
