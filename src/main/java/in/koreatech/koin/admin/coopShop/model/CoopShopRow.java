package in.koreatech.koin.admin.coopShop.model;

public record CoopShopRow(
    String coopShopName,
    String phone,
    String location,
    String remark,
    String type,
    String dayOfWeek,
    String openTime,
    String closeTime
) {

}
