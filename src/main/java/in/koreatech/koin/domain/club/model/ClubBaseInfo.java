package in.koreatech.koin.domain.club.model;

public record ClubBaseInfo(
    Integer clubId,
    String name,
    String category,
    Integer likes,
    String imageUrl,
    Boolean isLiked,
    Boolean isLikeHidden,
    Integer recruitmentPeriod,
    Boolean isAlwaysRecruiting
) {

}
