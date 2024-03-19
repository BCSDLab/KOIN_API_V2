package in.koreatech.koin.global.domain.upload;

import java.util.Arrays;

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
    ;

    public static ImageUploadDomain from(String domain) {
        return Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(domain))
            .findAny()
            .orElseThrow(() -> ImageUploadDomainNotFoundException.withDetail("domain: " + domain));
    }
}
