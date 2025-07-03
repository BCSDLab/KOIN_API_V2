package in.koreatech.koin._common.exception;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "표준 에러 응답 포맷 (CamelCase)")
public class ErrorResponse {

    @JsonIgnore
    @Schema(description = "HTTP 상태 코드", hidden = true)
    private final int status;

    @Schema(description = "비즈니스 에러 코드")
    private final String code;

    @Schema(description = "사용자에게 보여줄 에러 메시지")
    private final String message;

    @Schema(description = "에러 추적용 UUID")
    private final String errorTraceId;

    @Schema(description = "필드별 검증 오류 목록")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<FieldError> fieldErrors;

    public ErrorResponse(int status, String message, String errorTraceId) {
        this(status, "", message, errorTraceId, List.of());
    }

    public ErrorResponse(int status, String code, String message, String errorTraceId) {
        this(status, code, message, errorTraceId, List.of());
    }

    public ErrorResponse(int status, String code, String message, String errorTraceId, List<FieldError> fieldErrors) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.errorTraceId = errorTraceId;
        this.fieldErrors = fieldErrors;
    }

    public record FieldError(
        @Schema(description = "오류가 발생한 필드 이름")
        String field,

        @Schema(description = "해당 필드의 오류 메시지")
        String message,

        @Schema(description = "해당 필드의 오류 코드(제약조건 이름 등)")
        String constraint
    ) {

        public FieldError(String field, String message, String constraint) {
            this.field = field;
            this.message = message;
            this.constraint = constraint;
        }
    }
}
