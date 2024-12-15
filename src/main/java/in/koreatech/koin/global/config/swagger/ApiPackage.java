package in.koreatech.koin.global.config.swagger;

import lombok.Getter;

@Getter
public enum ApiPackage {
    LOGIN_API(new String[] {
        "/**/login"
    }),
    ADMIN_API(new String[] {
        "in.koreatech.koin.admin"
    }),
    BUSINESS_API(new String[] {
        "in.koreatech.koin.domain.owner",
        "in.koreatech.koin.domain.benefit",
        "in.koreatech.koin.domain.ownershop",
        "in.koreatech.koin.domain.shop",
        "in.koreatech.koin.domain.land"
    }),
    CAMPUS_API(new String[] {
        "in.koreatech.koin.domain.bus",
        "in.koreatech.koin.domain.community",
        "in.koreatech.koin.domain.coop",
        "in.koreatech.koin.domain.coopshop",
        "in.koreatech.koin.domain.dining"
    }),
    USER_API(new String[] {
        "in.koreatech.koin.domain.user",
        "in.koreatech.koin.domain.student",
        "in.koreatech.koin.domain.timetable",
        "in.koreatech.koin.domain.timetableV2"
    }),
    ABTEST_API(new String[] {
        "in.koreatech.koin.admin.abtest"
    }),
    BCSD_API(new String[] {
        "in.koreatech.koin.domain.activity",
        "in.koreatech.koin.domain.dept",
        "in.koreatech.koin.domain.kakao",
        "in.koreatech.koin.domain.member",
        "in.koreatech.koin.domain.version"
    });

    private final String[] paths;

    ApiPackage(String[] paths) {
        this.paths = paths;
    }
}

