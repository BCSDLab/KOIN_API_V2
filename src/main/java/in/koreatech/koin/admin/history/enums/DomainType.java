package in.koreatech.koin.admin.history.enums;

import lombok.Getter;

@Getter
public enum DomainType {
    MEMBERS("Member", "BCSDLAB 회원"),
    TRACKS("Track", "트랙"),
    TECH_STACKS("TechStack", "기술 스택"),
    NOTICES("Notice", "코인 공지"),
    VERSIONS("Version", "버전관리"),

    CATEGORIES("Categories", "카테고리"),

    BENEFITS("Benefit", "혜택"),
    BENEFIT_CATEGORIES("Benefit Categories", "혜택 카테고리"),

    SHOPS("Shop", "상점"),
    SHOPS_CATEGORIES("Shop Categories", "상점 카테고리"),

    MENUS("Menu", "메뉴"),
    MENUS_CATEGORIES("Menu Categories", "메뉴 카테고리"),

    REVIEWS("Review", "리뷰"),

    ABTESTS("Abtest", "AB 테스트"),

    LANDS("Land", "복덕방"),
    COOP_SHOPS("CoopShop", "생협 매장"),

    USERS("User", "회원"),
    STUDENTS("Student", "학생"),
    OWNERS("Owner", "사장님"),
    ADMINS("Admin", "어드민"),

    BANNERS("Banner", "배너"),
    BANNER_CATEGORIES("Banner Categories", "배너 카테고리"),

    CLUBS("Clubs", "동아리")
    ;

    private final String value;
    private final String description;

    DomainType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
