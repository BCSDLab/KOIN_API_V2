package in.koreatech.koin.admin.history.enums;

import lombok.Getter;

@Getter
public enum DomainType {
    MEMBERS("BCSDLAB 회원"),
    TRACKS("트랙"),
    TECHSTACKS("기술 스택"),
    NOTICE("코인 공지"),
    VERSION("버전관리"),

    CATEGORIES("카테고리"),

    BENEFIT("혜택"),
    BENEFITCATEGORIES("혜택 카테고리"),

    SHOPS("상점"),
    SHOPSCATEGORIES("상점 카테고리"),

    MENUS("메뉴"),
    MENUSCATEGORIES("메뉴 카테고리"),

    REVIEWS("리뷰"),

    ABTEST("AB 테스트"),

    LANDS("복덕방"),
    COOP_SHOPS("생협 매장"),

    USERS("회원"),
    STUDENT("학생"),
    OWNER("사장님"),
    ADMIN("어드민"),

    BANNERS("배너"),
    BANNER_CATEGORIES("배너 카테고리"),

    CLUBS("동아리"),

    KEYWORDS("키워드 알림"),

    LOST_ITEMS("분실물"),
    COOP_SEMESTER("생협 학기"),
    ;

    private final String description;

    DomainType(String description) {
        this.description = description;
    }
}
