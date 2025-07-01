package in.koreatech.koin._common.exception;

import java.time.DateTimeException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.catalina.connector.ClientAbortException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import in.koreatech.koin._common.auth.exception.AuthenticationException;
import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin._common.exception.custom.DataNotFoundException;
import in.koreatech.koin._common.exception.custom.DuplicationException;
import in.koreatech.koin._common.exception.custom.ExternalServiceException;
import in.koreatech.koin._common.exception.custom.KoinException;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin._common.exception.custom.KoinIllegalStateException;
import in.koreatech.koin._common.exception.custom.RequestTooFastException;
import in.koreatech.koin._common.exception.custom.TooManyRequestsException;
import in.koreatech.koin.domain.order.address.exception.AddressException;
import in.koreatech.koin.domain.order.cart.exception.CartException;
import in.koreatech.koin.domain.order.delivery.exception.DeliveryException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 커스텀 예외

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(
        HttpServletRequest request,
        CustomException e
    ) {
        String errorTraceId = requestLogging(request, e.getFullMessage());
        return buildErrorResponse(e.getErrorCode(), errorTraceId);
    }

    // 표준 예외 및 정의되어 있는 예외

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
        HttpServletRequest request,
        IllegalArgumentException e
    ) {
        String errorTraceId = requestLogging(request, e.getMessage());
        return buildErrorResponse(ApiResponseCode.ILLEGAL_ARGUMENT, errorTraceId);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(
        HttpServletRequest request,
        IllegalStateException e
    ) {
        String errorTraceId = requestLogging(request, e.getMessage());
        return buildErrorResponse(ApiResponseCode.ILLEGAL_STATE, errorTraceId);
    }

    @ExceptionHandler(DateTimeException.class)
    public ResponseEntity<Object> handleDateTimeParseException(
        HttpServletRequest request,
        DateTimeException e
    ) {
        String errorTraceId = requestLogging(request, e.getMessage());
        return buildErrorResponse(ApiResponseCode.INVALID_DATE_TIME, errorTraceId);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException e,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest webRequest
    ) {
        HttpServletRequest request = ((ServletWebRequest)webRequest).getRequest();
        log.warn("검증과정에서 문제가 발생했습니다. uri: {} {}, ", request.getMethod(), request.getRequestURI(), e);
        String errorTraceId = requestLogging(request, e.getMessage());
        String errorMessages = e.getBindingResult().getAllErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining("\n"));
        return buildErrorResponse(ApiResponseCode.INVALID_REQUEST_PAYLOAD, errorTraceId);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException e,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        String errorTraceId = requestLogging(((ServletWebRequest)request).getRequest(), e.getMessage());
        return buildErrorResponse(ApiResponseCode.NOT_READABLE_HTTP_MESSAGE, errorTraceId);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Object> handleUnsupportedOperationException(
        HttpServletRequest request,
        UnsupportedOperationException e
    ) {
        String errorTraceId = requestLogging(request, e.getMessage());
        return buildErrorResponse(ApiResponseCode.UNSUPPORTED_OPERATION, errorTraceId);
    }

    @ExceptionHandler(ClientAbortException.class)
    public ResponseEntity<Object> handleClientAbortException(
        HttpServletRequest request,
        ClientAbortException e
    ) {
        String errorTraceId = requestLogging(request, "클라이언트가 연결을 중단했습니다: " + e.getMessage());
        return buildErrorResponse(ApiResponseCode.CLIENT_ABORTED, errorTraceId);
    }

    @Override
    public ResponseEntity<Object> handleNoHandlerFoundException(
        NoHandlerFoundException e,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        log.warn("유효하지 않은 API 경로입니다. {}", e.getRequestURL());
        String errorTraceId = requestLogging(((ServletWebRequest)request).getRequest(), e.getMessage());
        return buildErrorResponse(ApiResponseCode.NO_HANDLER_FOUND, errorTraceId);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Object> handleOptimisticLockException(
        HttpServletRequest request,
        ObjectOptimisticLockingFailureException e
    ) {
        String errorTraceId = requestLogging(((ServletWebRequest)request).getRequest(), e.getMessage());
        return buildErrorResponse(ApiResponseCode.OPTIMISTIC_LOCKING_FAILURE, errorTraceId);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(
        HttpServletRequest request,
        Exception e
    ) {
        String errorMessage = e.getMessage();
        String errorFile = e.getStackTrace()[0].getFileName();
        int errorLine = e.getStackTrace()[0].getLineNumber();
        String errorName = e.getClass().getSimpleName();
        String detail = String.format("""
                Exception: *%s*
                Location: *%s Line %d*
                                
                ```%s```
                """,
            errorName, errorFile, errorLine, errorMessage);
        log.error("""
            서버에서 에러가 발생했습니다. uri: {} {}
            {}
            """, request.getMethod(), request.getRequestURI(), detail);
        String errorTraceId = requestLogging(request, errorMessage);
        return buildErrorResponse(ApiResponseCode.INTERNAL_SERVER_ERROR, errorTraceId);
    }

    // 예외 메시지 구성 로직
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception e,
        @Nullable Object body,
        HttpHeaders headers,
        HttpStatusCode statusCode,
        WebRequest request
    ) {
        return buildErrorResponse(HttpStatus.valueOf(statusCode.value()), e.getMessage());
    }

    private String requestLogging(HttpServletRequest request, String errorMessage) {
        UUID errorTraceId = UUID.randomUUID();
        log.warn(errorMessage);
        log.warn("traceId: {}", errorTraceId);
        log.info("request header: {}", getHeaders(request));
        log.info("request query string: {}", getQueryString(request));
        log.info("request body: {}", getRequestBody(request));
        return errorTraceId.toString();
    }

    private ResponseEntity<Object> buildErrorResponse(ApiResponseCode errorCode, String errorTraceId) {
        ErrorResponse response = new ErrorResponse(
            errorCode.getHttpStatus().value(),
            errorCode.getCode(),
            errorCode.getMessage(),
            errorTraceId
        );
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    private Map<String, Object> getHeaders(HttpServletRequest request) {
        Map<String, Object> headerMap = new HashMap<>();
        Enumeration<String> headerArray = request.getHeaderNames();
        while (headerArray.hasMoreElements()) {
            String headerName = headerArray.nextElement();
            headerMap.put(headerName, request.getHeader(headerName));
        }
        return headerMap;
    }

    private String getQueryString(HttpServletRequest httpRequest) {
        String queryString = httpRequest.getQueryString();
        if (queryString == null) {
            return " - ";
        }
        return queryString;
    }

    private String getRequestBody(HttpServletRequest request) {
        var wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper == null) {
            return " - ";
        }
        try {
            // body 가 읽히지 않고 예외처리 되는 경우에 캐시하기 위함
            wrapper.getInputStream().readAllBytes();
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length == 0) {
                return " - ";
            }
            return new String(buf, wrapper.getCharacterEncoding());
        } catch (Exception e
        ) {
            return " - ";
        }
    }

    // Deprecated : 아래 코드부터는 에러코드 작업을 하며 없어질 예정입니다.
    @ExceptionHandler(KoinException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
        HttpServletRequest request,
        KoinException e
    ) {
        requestLogging(request, e.getFullMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(KoinIllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
        HttpServletRequest request,
        KoinIllegalArgumentException e
    ) {
        requestLogging(request, e.getFullMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(KoinIllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(
        HttpServletRequest request,
        KoinIllegalStateException e
    ) {
        requestLogging(request, e.getFullMessage());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<Object> handleAuthorizationException(
        HttpServletRequest request,
        AuthorizationException e
    ) {
        requestLogging(request, e.getFullMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(
        HttpServletRequest request,
        AuthenticationException e
    ) {
        requestLogging(request, e.getFullMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<Object> handleDataNotFoundException(
        HttpServletRequest request,
        DataNotFoundException e
    ) {
        requestLogging(request, e.getFullMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(DuplicationException.class)
    public ResponseEntity<Object> handleDataNotFoundException(
        HttpServletRequest request,
        DuplicationException e
    ) {
        requestLogging(request, e.getFullMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Object> handleExternalServiceException(
        HttpServletRequest request,
        ExternalServiceException e
    ) {
        requestLogging(request, e.getFullMessage());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Object> handleKoinRequestTooManyException(
        HttpServletRequest request,
        TooManyRequestsException e
    ) {
        requestLogging(request, e.getFullMessage());
        return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
    }

    @ExceptionHandler(RequestTooFastException.class)
    public ResponseEntity<Object> handleRequestTooFastException(
        HttpServletRequest request,
        RequestTooFastException e
    ) {
        requestLogging(request, e.getFullMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    private ResponseEntity<Object> buildErrorResponse(
        HttpStatus httpStatus,
        String message
    ) {
        String errorTraceId = UUID.randomUUID().toString();
        log.warn("traceId: {}", errorTraceId);
        var response = new ErrorResponse(httpStatus.value(), message, errorTraceId);
        return ResponseEntity.status(httpStatus).body(response);
    }

    private ResponseEntity<Object> buildErrorResponseWithErrorCode(
        HttpStatus httpStatus,
        String message,
        String errorCode
    ) {
        String errorTraceId = UUID.randomUUID().toString();
        log.warn("traceId: {}", errorTraceId);
        var response = new ErrorResponse(httpStatus.value(), errorCode, message, errorTraceId);
        return ResponseEntity.status(httpStatus).body(response);
    }

    // 공통 에러 코드 + 예외 적용 전 임시 처리
    @ExceptionHandler(AddressException.class)
    public ResponseEntity<Object> handleAddressApiException(
        HttpServletRequest request,
        AddressException e
    ) {
        log.warn(e.getFullMessage());
        requestLogging(request, e.getFullMessage());
        return buildErrorResponseWithErrorCode(
            HttpStatus.valueOf(e.getErrorCode().getHttpIntegerCode()),
            e.getFullMessage(),
            e.getErrorCode().name()
        );
    }

    // 공통 에러 코드 + 예외 적용 전 임시 처리
    @ExceptionHandler(DeliveryException.class)
    public ResponseEntity<Object> handleAddressApiException(
        HttpServletRequest request,
        DeliveryException e
    ) {
        log.warn(e.getFullMessage());
        requestLogging(request, e.getFullMessage());
        return buildErrorResponseWithErrorCode(
            HttpStatus.valueOf(e.getErrorCode().getHttpIntegerCode()),
            e.getFullMessage(),
            e.getErrorCode().name()
        );
    }


    // 공통 에러 코드 + 예외 적용 전 임시 처리
    @ExceptionHandler(CartException.class)
    public ResponseEntity<Object> handleCartException(
        HttpServletRequest request,
        CartException e
    ) {
        log.warn(e.getFullMessage());
        requestLogging(request, e.getFullMessage());
        return buildErrorResponseWithErrorCode(
            HttpStatus.valueOf(e.getErrorCode().getHttpIntegerCode()),
            e.getFullMessage(),
            e.getErrorCode().name()
        );
    }
}
