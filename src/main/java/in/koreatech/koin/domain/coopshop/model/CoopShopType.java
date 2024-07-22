package in.koreatech.koin.domain.coopshop.model;

import java.util.Arrays;

import in.koreatech.koin.domain.coopshop.exception.CoopShopNotFoundException;
import lombok.Getter;

@Getter
public enum CoopShopType {
    CAFETERIA("학생식당");

    private final String name;

    CoopShopType(String name) {
        this.name = name;
    }

    public CoopShopType from(String name) {
        return Arrays.stream(CoopShopType.values())
            .filter(coopShop -> coopShop.name().equals(name))
            .findAny()
            .orElseThrow(() -> new CoopShopNotFoundException(name));
    }
}
