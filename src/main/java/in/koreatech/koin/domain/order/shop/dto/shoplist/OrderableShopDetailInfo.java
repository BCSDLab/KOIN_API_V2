package in.koreatech.koin.domain.order.shop.dto.shoplist;

import java.util.List;
import java.util.Map;

public record OrderableShopDetailInfo (
    Map<Integer, List<Integer>> shopCategories,
    Map<Integer, List<String>> shopImages,
    Map<Integer, List<OrderableShopsResponse.ShopOpenInfo>> shopOpens,
    Map<Integer, OrderableShopOpenStatus> shopOpenStatus
) {

}
