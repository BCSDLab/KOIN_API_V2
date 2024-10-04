package in.koreatech.koin.domain.coopshop.model;

import java.util.Arrays;

import in.koreatech.koin.domain.coopshop.exception.CoopShopNotFoundException;
import lombok.Getter;

@Getter
public enum CoopShopType {
    CAFETERIA("학생식당"),
    WELFARE_CENTER("복지관"),
    _2CAMPUS("2캠퍼스"),
    WELFARE_CVS("복지관 편의점"),
    CHAMBIT_CVS("참빛관 편의점"),
    BOOK_STORE("서점"),
    DAZZLE("대즐"),
    LAUNDRY("세탁소"),
    BEAUTY_SHOP("미용실"),
    POST_OFFICE("우편취급국"),
    OPTICIAN("안경원"),
    COPY_ROOM("복사실"),
    ARCADE("오락실");

    private final String coopShopName;

    CoopShopType(String coopShopName) {
        this.coopShopName = coopShopName;
    }

    public static CoopShopType from(String name) {
        return Arrays.stream(CoopShopType.values())
            .filter(coopShop -> coopShop.getCoopShopName().equals(name))
            .findAny()
            .orElseThrow(() -> new CoopShopNotFoundException(name));
    }
}
