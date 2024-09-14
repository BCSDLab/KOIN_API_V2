package in.koreatech.koin.global.exception;

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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 커스텀 예외

    @ExceptionHandler(KoinException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
        HttpServletRequest request,
        KoinException e
    ) {
        log.warn(e.getFullMessage());
        requestLogging(request);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(KoinIllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
        HttpServletRequest request,
        KoinIllegalArgumentException e
    ) {
        log.warn(e.getFullMessage());
        requestLogging(request);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(KoinIllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(
        HttpServletRequest request,
        KoinIllegalStateException e
    ) {
        log.warn(e.getFullMessage());
        requestLogging(request);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<Object> handleAuthorizationException(
        HttpServletRequest request,
        AuthorizationException e
    ) {
        log.warn(e.getFullMessage());
        requestLogging(request);
        return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(
        HttpServletRequest request,
        AuthenticationException e
    ) {
        log.warn(e.getFullMessage());
        requestLogging(request);
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<Object> handleDataNotFoundException(
        HttpServletRequest request,
        DataNotFoundException e
    ) {
        log.warn(e.getFullMessage());
        requestLogging(request);
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(DuplicationException.class)
    public ResponseEntity<Object> handleDataNotFoundException(
        HttpServletRequest request,
        DuplicationException e
    ) {
        log.warn(e.getFullMessage());
        requestLogging(request);
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Object> handleExternalServiceException(
        HttpServletRequest request,
        ExternalServiceException e
    ) {
        log.warn(e.getFullMessage());
        requestLogging(request);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    // 표준 예외 및 정의되어 있는 예외

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleRequestTooFastException(
        HttpServletRequest request,
        IllegalArgumentException e
    ) {
        log.warn(e.getMessage());
        requestLogging(request);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleRequestTooFastException(
        HttpServletRequest request,
        IllegalStateException e
    ) {
        log.warn(e.getMessage());
        requestLogging(request);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(RequestTooFastException.class)
    public ResponseEntity<Object> handleRequestTooFastException(
        HttpServletRequest request,
        RequestTooFastException e
    ) {
        log.warn(e.getMessage());
        requestLogging(request);
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(DateTimeException.class)
    public ResponseEntity<Object> handleDateTimeParseException(
        HttpServletRequest request,
        DateTimeException e
    ) {
        log.warn(e.getMessage());
        requestLogging(request);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "잘못된 날짜 형식입니다.");
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Object> handleUnsupportedOperationException(
        HttpServletRequest request,
        UnsupportedOperationException e
    ) {
        log.warn(e.getMessage());
        requestLogging(request);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "지원하지 않는 API 입니다.");
    }

    @ExceptionHandler(ClientAbortException.class)
    public ResponseEntity<Object> handleClientAbortException(
        HttpServletRequest request,
        ClientAbortException e
    ) {
        logger.warn("클라이언트가 연결을 중단했습니다: " + e.getMessage());
        requestLogging(request);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "클라이언트에 의해 연결이 중단되었습니다");
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
        requestLogging(request);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 오류가 발생했습니다.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest webRequest
    ) {
        HttpServletRequest request = ((ServletWebRequest)webRequest).getRequest();
        log.warn("검증과정에서 문제가 발생했습니다. uri: {} {}, ", request.getMethod(), request.getRequestURI(), ex);
        requestLogging(request);
        String errorMessages = ex.getBindingResult().getAllErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMessages);
    }

    // 예외 메시지 구성 로직
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception ex,
        Object body,
        HttpHeaders headers,
        HttpStatusCode statusCode,
        WebRequest request
    ) {
        return buildErrorResponse(HttpStatus.valueOf(statusCode.value()), ex.getMessage());
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

    private void requestLogging(HttpServletRequest request) {
        log.info("request header: {}", getHeaders(request));
        log.info("request query string: {}", getQueryString(request));
        log.info("request body: {}", getRequestBody(request));
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
}
