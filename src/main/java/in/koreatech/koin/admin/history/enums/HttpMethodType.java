package in.koreatech.koin.admin.history.enums;

import lombok.Getter;

@Getter
public enum HttpMethodType {
    POST("생성"),
    PUT("수정"),
    DELETE("삭제"),
    PATCH("수정"),
    ;

    private final String value;

    HttpMethodType(String value) {
        this.value = value;
    }
}
