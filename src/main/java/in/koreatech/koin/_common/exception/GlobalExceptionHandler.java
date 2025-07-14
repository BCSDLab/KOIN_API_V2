package in.koreatech.koin._common.exception;

import java.time.DateTimeException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
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
import in.koreatech.koin.domain.order.address.exception.AddressException;
import in.koreatech.koin.domain.order.cart.exception.CartException;
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
        return buildErrorResponse(request, e.getErrorCode(), e.getFullMessage());
    }

    // 표준 예외 및 정의되어 있는 예외

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
        HttpServletRequest request,
        IllegalArgumentException e
    ) {
        return buildErrorResponse(request, ApiResponseCode.ILLEGAL_ARGUMENT, e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(
        HttpServletRequest request,
        IllegalStateException e
    ) {
        return buildErrorResponse(request, ApiResponseCode.ILLEGAL_STATE, e.getMessage());
    }

    @ExceptionHandler(DateTimeException.class)
    public ResponseEntity<Object> handleDateTimeParseException(
        HttpServletRequest request,
        DateTimeException e
    ) {
        return buildErrorResponse(request, ApiResponseCode.INVALID_DATE_TIME, e.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatusCode statusCode,
        WebRequest webRequest
    ) {
        HttpServletRequest request = ((ServletWebRequest)webRequest).getRequest();
        ApiResponseCode errorCode = ApiResponseCode.INVALID_REQUEST_BODY;
        String errorTraceId = UUID.randomUUID().toString();

        List<ErrorResponse.FieldError> fieldErrors = getFieldErrors(ex);
        String firstErrorMessage = getFirstFieldErrorMessage(fieldErrors, errorCode.getMessage());

        requestLogging(request, errorCode.getHttpStatus().value(), firstErrorMessage, errorTraceId);

        ErrorResponse body = new ErrorResponse(
            errorCode.getHttpStatus().value(),
            errorCode.getCode(),
            firstErrorMessage,
            errorTraceId,
            fieldErrors
        );
        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException e,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest webRequest
    ) {
        HttpServletRequest request = ((ServletWebRequest)webRequest).getRequest();
        if (e.getMostSpecificCause() instanceof CustomException ce) {
            return handleCustomException(request, ce);
        }
        return buildErrorResponse(request, ApiResponseCode.NOT_READABLE_HTTP_MESSAGE, e.getMessage());
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Object> handleUnsupportedOperationException(
        HttpServletRequest request,
        UnsupportedOperationException e
    ) {
        return buildErrorResponse(request, ApiResponseCode.UNSUPPORTED_OPERATION, e.getMessage());
    }

    @ExceptionHandler(ClientAbortException.class)
    public ResponseEntity<Object> handleClientAbortException(
        HttpServletRequest request,
        ClientAbortException e
    ) {
        return buildErrorResponse(request, ApiResponseCode.CLIENT_ABORTED, e.getMessage());
    }

    @Override
    public ResponseEntity<Object> handleNoHandlerFoundException(
        NoHandlerFoundException e,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest webRequest
    ) {
        HttpServletRequest request = ((ServletWebRequest)webRequest).getRequest();
        return buildErrorResponse(request, ApiResponseCode.NO_HANDLER_FOUND, e.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Object> handleOptimisticLockException(
        HttpServletRequest request,
        ObjectOptimisticLockingFailureException e
    ) {
        return buildErrorResponse(request, ApiResponseCode.OPTIMISTIC_LOCKING_FAILURE, e.getMessage());
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
        return buildErrorResponse(request, ApiResponseCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    // 예외 메시지 구성 로직
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception e,
        @Nullable Object body,
        HttpHeaders headers,
        HttpStatusCode statusCode,
        WebRequest webRequest
    ) {
        HttpServletRequest request = ((ServletWebRequest)webRequest).getRequest();
        return buildErrorResponse(request, HttpStatus.valueOf(statusCode.value()), e.getMessage());
    }

    private void requestLogging(
        HttpServletRequest request,
        int httpStatus,
        String errorMessage,
        String errorTraceId
    ) {
        log.warn("[{}] {} | errorTraceId={}", httpStatus, errorMessage, errorTraceId);
        log.debug("Request: {} {}", request.getMethod(), request.getRequestURI());
        log.debug("Headers: {}", getHeaders(request));
        log.debug("Query String: {}", getQueryString(request));
        log.debug("Body: {}", getRequestBody(request));
    }

    private ResponseEntity<Object> buildErrorResponse(
        HttpServletRequest request,
        ApiResponseCode errorCode,
        String errorMessage
    ) {
        String errorTraceId = UUID.randomUUID().toString();
        requestLogging(request, errorCode.getHttpStatus().value(), errorMessage, errorTraceId);

        ErrorResponse response = new ErrorResponse(
            errorCode.getHttpStatus().value(),
            errorCode.getCode(),
            errorCode.getMessage(),
            errorTraceId
        );
        return ResponseEntity.status(response.status()).body(response);
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

    private List<ErrorResponse.FieldError> getFieldErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::toFieldError)
            .toList();
    }

    private ErrorResponse.FieldError toFieldError(FieldError fe) {
        String field = fe.getField();
        String constraint = Objects.requireNonNull(fe.getCode());
        String message = Objects.requireNonNullElse(
            fe.getDefaultMessage(), ApiResponseCode.INVALID_REQUEST_BODY.getMessage()
        );

        return new ErrorResponse.FieldError(field, message, constraint);
    }

    private String getFirstFieldErrorMessage(List<ErrorResponse.FieldError> fields, String defaultMessage) {
        if (fields.isEmpty()) {
            return defaultMessage;
        }
        return fields.get(0).message();
    }

    // Deprecated : 아래 코드부터는 에러코드 작업을 하며 없어질 예정입니다.
    @ExceptionHandler(KoinException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
        HttpServletRequest request,
        KoinException e
    ) {
        return buildErrorResponse(request, HttpStatus.BAD_REQUEST, e.getFullMessage());
    }

    @ExceptionHandler(KoinIllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
        HttpServletRequest request,
        KoinIllegalArgumentException e
    ) {
        return buildErrorResponse(request, HttpStatus.BAD_REQUEST, e.getFullMessage());
    }

    @ExceptionHandler(KoinIllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(
        HttpServletRequest request,
        KoinIllegalStateException e
    ) {
        return buildErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getFullMessage());
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<Object> handleAuthorizationException(
        HttpServletRequest request,
        AuthorizationException e
    ) {
        return buildErrorResponse(request, HttpStatus.FORBIDDEN, e.getFullMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(
        HttpServletRequest request,
        AuthenticationException e
    ) {
        return buildErrorResponse(request, HttpStatus.UNAUTHORIZED, e.getFullMessage());
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<Object> handleDataNotFoundException(
        HttpServletRequest request,
        DataNotFoundException e
    ) {
        return buildErrorResponse(request, HttpStatus.NOT_FOUND, e.getFullMessage());
    }

    @ExceptionHandler(DuplicationException.class)
    public ResponseEntity<Object> handleDataNotFoundException(
        HttpServletRequest request,
        DuplicationException e
    ) {
        return buildErrorResponse(request, HttpStatus.CONFLICT, e.getFullMessage());
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Object> handleExternalServiceException(
        HttpServletRequest request,
        ExternalServiceException e
    ) {
        return buildErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getFullMessage());
    }

    // 공통 에러 코드 + 예외 적용 전 임시 처리
    @ExceptionHandler(AddressException.class)
    public ResponseEntity<Object> handleAddressApiException(
        HttpServletRequest request,
        AddressException e
    ) {
        return buildErrorResponseWithErrorCode(
            request,
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
        return buildErrorResponseWithErrorCode(
            request,
            HttpStatus.valueOf(e.getErrorCode().getHttpIntegerCode()),
            e.getFullMessage(),
            e.getErrorCode().name()
        );
    }

    private ResponseEntity<Object> buildErrorResponse(
        HttpServletRequest request,
        HttpStatus httpStatus,
        String errorMessage
    ) {
        String errorTraceId = UUID.randomUUID().toString();
        requestLogging(request, httpStatus.value(), errorMessage, errorTraceId);
        var response = new ErrorResponse(httpStatus.value(), errorMessage, errorTraceId);
        return ResponseEntity.status(httpStatus).body(response);
    }

    private ResponseEntity<Object> buildErrorResponseWithErrorCode(
        HttpServletRequest request,
        HttpStatus httpStatus,
        String errorMessage,
        String errorCode
    ) {
        String errorTraceId = UUID.randomUUID().toString();
        requestLogging(request, httpStatus.value(), errorMessage, errorTraceId);
        var response = new ErrorResponse(httpStatus.value(), errorCode, errorMessage, errorTraceId);
        return ResponseEntity.status(httpStatus).body(response);
    }
}
