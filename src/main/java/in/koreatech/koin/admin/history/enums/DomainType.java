package in.koreatech.koin.admin.history.enums;

import lombok.Getter;

@Getter
public enum DomainType {
    MEMBERS("Member", "BCSDLAB 회원"),
    TRACKS("Track", "트랙"),
    TECHSTACKS("TechStack", "기술 스택"),
    NOTICE("Notice", "코인 공지"),
    VERSION("Version", "버전관리"),

    BENEFIT("Benefit", "혜택"),
    SHOPS("Shop", "상점"),
    REVIEWS("Review", "리뷰"),

    ABTEST("Abtest", "AB 테스트"),

    LANDS("Land", "복덕방"),
    COOPSHOP("CoopShop", "생협 매장"),

    USERS("User", "회원"),
    STUDENT("Student", "학생"),
    OWNER("Owner", "사장님"),
    ;

    private final String value;
    private final String description;

    DomainType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
