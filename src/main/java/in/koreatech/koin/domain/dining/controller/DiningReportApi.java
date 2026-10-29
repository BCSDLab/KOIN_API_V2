package in.koreatech.koin.domain.dining.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static in.koreatech.koin.global.code.ApiResponseCode.CREATED;
import static in.koreatech.koin.global.code.ApiResponseCode.DINING_ALREADY_SOLD_OUT;
import static in.koreatech.koin.global.code.ApiResponseCode.DINING_REPORT_ALREADY_SUBMITTED;
import static in.koreatech.koin.global.code.ApiResponseCode.DINING_REPORT_DATE_NOT_ALLOWED;
import static in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_STUDENT;
import static in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_USER_TYPE;
import static in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_WEB_ORIGIN;
import static in.koreatech.koin.global.code.ApiResponseCode.ILLEGAL_ARGUMENT;
import static in.koreatech.koin.global.code.ApiResponseCode.IMAGE_STORAGE_UNAVAILABLE;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_CSRF_TOKEN;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REPORT_IMAGE;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_DINING;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_READABLE_HTTP_MESSAGE;
import static in.koreatech.koin.global.code.ApiResponseCode.OPTIMISTIC_LOCKING_FAILURE;
import static in.koreatech.koin.global.code.ApiResponseCode.UNAUTHORIZED_USER;
import static in.koreatech.koin.global.code.ApiResponseCode.WITHDRAWN_USER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.dining.dto.DiningReportCreateRequest;
import in.koreatech.koin.domain.dining.dto.DiningReportCreateResponse;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Dining: 식단 품절 제보")
@SecurityRequirement(name = "Jwt Authentication")
public interface DiningReportApi {

    @ApiResponseCodes({
        CREATED,
        ILLEGAL_ARGUMENT,
        INVALID_REQUEST_BODY,
        NOT_READABLE_HTTP_MESSAGE,
        DINING_REPORT_DATE_NOT_ALLOWED,
        INVALID_REPORT_IMAGE,
        UNAUTHORIZED_USER,
        WITHDRAWN_USER,
        FORBIDDEN_USER_TYPE,
        FORBIDDEN_STUDENT,
        FORBIDDEN_WEB_ORIGIN,
        INVALID_CSRF_TOKEN,
        NOT_FOUND_DINING,
        DINING_ALREADY_SOLD_OUT,
        DINING_REPORT_ALREADY_SUBMITTED,
        OPTIMISTIC_LOCKING_FAILURE,
        IMAGE_STORAGE_UNAVAILABLE
    })
    @Operation(summary = "식단 품절 제보 등록", description = """
        - 인증된 STUDENT가 한국시간 당일 식단을 학생당 한 번 제보합니다. 다른 학생은 같은 식단을 제보할 수 있습니다.
        - 새 제보는 품절된 식단에 접수하지 않습니다. 제보 등록은 식단을 품절 처리하지 않습니다.
        - 공용 COOP 업로드의 file_url을 image_url로 전달하면 원본 URL을 그대로 보관합니다.
        - 동일 학생이 같은 식단을 다시 제보하면 사진 URL이 같아도 409를 반환합니다. 처리 후에도 재제보할 수 없습니다.
        """)
    @PostMapping("/dinings/{diningId}/soldout-reports")
    ResponseEntity<DiningReportCreateResponse> createReport(
        @Auth(permit = STUDENT) Integer userId,
        @Parameter(description = "한국시간 당일 식단 ID", example = "1", required = true)
        @PathVariable Integer diningId,
        @Valid @RequestBody DiningReportCreateRequest request
    );
}
