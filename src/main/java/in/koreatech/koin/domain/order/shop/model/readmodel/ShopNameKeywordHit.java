package in.koreatech.koin.domain.order.shop.model.readmodel;

/**
 * QueryDSL Projection Read Model.
 * 주문 가능 상점 이름 검색용 쿼리 모델 입니다.
 * 다음 데이터베이스 테이블에서 정보를 조회하여 취합합니다. 테이블 명(엔티티 명)
 * orderable_shop(OrderableShop): 주문 가능 상점 식별자
 * shops(Shop): 상점 이름
 * */
public record ShopNameKeywordHit(
    Integer orderableShopId,

    String orderableShopName
) {

}
