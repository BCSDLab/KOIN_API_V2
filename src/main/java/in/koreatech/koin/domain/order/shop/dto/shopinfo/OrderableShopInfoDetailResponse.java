package in.koreatech.koin.domain.order.shop.dto.shopinfo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.shop.model.domain.ShopBaseDeliveryTipRange;
import in.koreatech.koin.domain.order.shop.model.entity.OrderableShop;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.shop.ShopOpen;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OrderableShopInfoDetailResponse(
    @Schema(description = "상점 ID", example = "14")
    Integer shopId,
    @Schema(description = "주문 가능 상점 ID", example = "1")
    Integer orderableShopId,
    @Schema(description = "상점 이름", example = "멕시카나 치킨 - 병천점")
    String name,
    @Schema(description = "상점 주소", example = "충청남도 천안시 동남구 병천면 병천리 163-12")
    String address,
    @Schema(description = "상점 영업 시작 시간", example = "11:30")
    @DateTimeFormat(pattern = "HH:mm")
    LocalTime openTime,
    @Schema(description = "상점 영업 종료 시간", example = "20:30")
    @DateTimeFormat(pattern = "HH:mm")
    LocalTime closeTime,
    @Schema(description = "상점 휴무일", example = "[\"TUESDAY\", \"SUNDAY\"]")
    List<String> closedDays,
    @Schema(description = "상점 전화 번호", example = "010-1234-5678")
    String phone,
    @Schema(description = "가게 소개", example = "안녕하세요 맛있는 족발입니다. 고객님에게 신선한 음식을 제공합니다")
    String introduction,
    @Schema(description = "가게 알림", example = "*행사 이벤트 진행중입니다*\\n단체 주문 시 20% 할인 합니다.")
    String notice,
    List<DeliveryTips> deliveryTips,
    OwnerInfo ownerInfo,
    String origin
) {

    public static OrderableShopInfoDetailResponse from(OrderableShop orderableShop) {
        List<ShopOpen> shopOpens = orderableShop.getShop().getShopOpens();
        String today = LocalDate.now().getDayOfWeek().toString();

        Optional<ShopOpen> todayShopOpen = shopOpens.stream()
            .filter(shopOpen -> shopOpen.getDayOfWeek().equals(today))
            .findFirst();

        LocalTime openTime = todayShopOpen.map(ShopOpen::getOpenTime).orElse(null);
        LocalTime closeTime = todayShopOpen.map(ShopOpen::getCloseTime).orElse(null);

        List<String> closedDays = shopOpens.stream()
            .filter(ShopOpen::isClosed)
            .map(ShopOpen::getDayOfWeek)
            .collect(Collectors.toList());

        return new OrderableShopInfoDetailResponse(
            orderableShop.getShop().getId(),
            orderableShop.getId(),
            orderableShop.getShop().getName(),
            orderableShop.getShop().getAddress(),
            openTime,
            closeTime,
            closedDays,
            orderableShop.getShop().getPhone(),
            orderableShop.getShop().getIntroduction(),
            orderableShop.getShop().getNotice(),
            DeliveryTips.from(orderableShop.getShop().getBaseDeliveryTips().getDeliveryTipRanges()),
            OwnerInfo.from(orderableShop.getShop().getOwner(), orderableShop.getShop().getName(),
                orderableShop.getShop().getAddress()),
            orderableShop.getShop().getOrigin().getOrigin()
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record DeliveryTips(
        @Schema(description = "주문 금액 하한", example = "10000")
        Integer fromAmount,
        @Schema(description = "주문 금액 상한", example = "10000")
        Integer toAmount,
        @Schema(description = "주문 금액 구간별 배달팁", example = "2500")
        Integer fee
    ) {

        public static DeliveryTips from(ShopBaseDeliveryTipRange range) {
            return new DeliveryTips(
                range.fromAmount(),
                range.toAmount(),
                range.fee()
            );
        }

        public static List<DeliveryTips> from(List<ShopBaseDeliveryTipRange> ranges) {
            return ranges.stream()
                .map(DeliveryTips::from)
                .collect(Collectors.toList());
        }
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record OwnerInfo(
        @Schema(description = "사장님 이름", example = "홍길동")
        String name,
        @Schema(description = "상호명", example = "맛있는 족발 - 병천점")
        String shopName,
        @Schema(description = "사업자 주소", example = "충청남도 천안시 동남구 190 -12")
        String address,
        @Schema(description = "사업자 등록번호", example = "홍길동")
        String companyRegistrationNumber
    ) {

        public static OwnerInfo from(Owner owner, String shopName, String address) {
            if (owner == null) {
                return new OwnerInfo(null, null, null, null);
            }

            String ownerName = Optional.ofNullable(owner.getUser())
                .map(User::getName)
                .orElse(null);

            return new OwnerInfo(
                ownerName,
                shopName,
                address,
                owner.getCompanyRegistrationNumber()
            );
        }
    }
}
