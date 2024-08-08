package in.koreatech.koin.domain.shop.model;

public enum ReportStatus {
    UNHANDLED("UNHANDLED", "처리 안함"),
    DISMISSED("DISMISSED", "무의미한 신고여서 관련 리뷰 유지"),
    DELETED("DELETED", "유의미한 신고여서 관련 리뷰 삭제"),
    ;

    private final String value;
    private final String description;

    ReportStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
