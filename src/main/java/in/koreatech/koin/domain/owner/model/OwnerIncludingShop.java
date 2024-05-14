package in.koreatech.koin.domain.owner.model;

import in.koreatech.koin.domain.shop.model.Shop;
import lombok.Getter;

@Getter
public class OwnerIncludingShop {

    private Owner owner;
    private Integer shop_id;
    private String shop_name;

    public OwnerIncludingShop(Owner owner, Integer shop_id, String shop_name) {
        this.owner = owner;
        this.shop_id = shop_id;
        this.shop_name = shop_name;
    }

    public static OwnerIncludingShop of(Owner owner, Shop shop) {
        return new OwnerIncludingShop(
            owner,
            shop.getId(),
            shop.getName()
        );
    }

    public static OwnerIncludingShop of(Owner owner) {
        return new OwnerIncludingShop(
            owner,
            null,
            null
        );
    }
}
