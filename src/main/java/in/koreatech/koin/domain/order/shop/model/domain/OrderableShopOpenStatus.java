package in.koreatech.koin.domain.order.shop.model.domain;

public enum OrderableShopOpenStatus {

    OPERATING("영업중"),
    CLOSED("영업 종료"),
    PREPARING("영업 준비중");

    private String status;

    OrderableShopOpenStatus(String status) {
        this.status = status;
    }
}
