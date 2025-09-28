package in.koreatech.koin.domain.order.shop.model.readmodel;

/**
 * QueryDSL Projection Read Model.
 * 주문 가능 상점 데이터 조회용 쿼리 모델 입니다.
 * 다음 데이터베이스 테이블에서 정보를 조회하여 취합합니다. 테이블 명(엔티티 명)
 * shops(Shop): 상점 식별자, 상점 이름
 * orderable_shop(OrderableShop): 배달 가능 여부, 포장 가능 여부, 이벤트 진행 여부, 최소 주문 금액
 * shop_reviews(ShopReview): 리뷰 개수(count), 리뷰 별점 평균(avg)
 * shop_base_delivery_tip(ShopBaseDeliveryTip): 최소 배달비, 최대 배달비
 * shop_operation(ShopOperation): 상점 영업 정보 (is_open이 true 인 경우에만 주문 가능 합니다)
 * */
public record OrderableShopBaseInfo(
    Integer shopId,
    Integer orderableShopId,
    String name,
    Boolean isDeliveryAvailable,
    Boolean isTakeoutAvailable,
    Boolean serviceEvent,
    Integer minimumOrderAmount,
    Double ratingAverage,
    Long reviewCount,
    Integer minimumDeliveryTip,
    Integer maximumDeliveryTip,
    Boolean isOpen
) {

}
