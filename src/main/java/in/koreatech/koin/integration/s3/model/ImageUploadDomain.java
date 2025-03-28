package in.koreatech.koin.integration.s3.model;

import java.util.Arrays;

import in.koreatech.koin.integration.s3.exception.ImageUploadDomainNotFoundException;

/**
 * 이미지 업로드 시 사용되는 도메인별 디렉터리 명
 */
public enum ImageUploadDomain {
    ITEMS,
    LANDS,
    CIRCLES,
    MARKET,
    SHOPS,
    MEMBERS,
    OWNERS,
    COOP,
    ADMIN,
    LOST_ITEMS,
    BANNER,
    ;

    public static ImageUploadDomain from(String domain) {
        return Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(domain))
            .findAny()
            .orElseThrow(() -> ImageUploadDomainNotFoundException.withDetail("domain: " + domain));
    }
}
