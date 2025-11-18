package in.koreatech.koin.domain.shop.repository.shop.dto;

/**
 * QueryDSL Projection Read Model.
 * 주변 상점 이름 검색용 쿼리 모델 입니다.
 * 다음 데이터베이스 테이블에서 정보를 조회하여 취합합니다.
 * shops(Shop): 상점 식별자, 상점 이름,
 * */
public record ShopNameKeywordHit(
    Integer shopId,
    String shopName
) {
}
