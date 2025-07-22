package in.koreatech.koin.domain.order.shop.dto.shoplist;

import java.util.List;
import java.util.Map;

import in.koreatech.koin.domain.order.shop.model.domain.OrderableShopOpenStatus;

public record OrderableShopDetailInfo (
    Map<Integer, List<Integer>> shopCategories,
    Map<Integer, List<String>> shopImages,
    Map<Integer, List<OrderableShopOpenInfo>> shopOpens,
    Map<Integer, OrderableShopOpenStatus> shopOpenStatus
) {

}
