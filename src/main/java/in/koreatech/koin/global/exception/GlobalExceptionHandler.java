package in.koreatech.koin.global.exception;

import java.time.format.DateTimeParseException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        HttpServletRequest request,
        IllegalArgumentException e
    ) {
        log.warn(e.getMessage());
        requestLogging(request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.from("요청 파라미터가 잘못되었습니다."));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleAuthorizationException(
        HttpServletRequest request,
        AuthorizationException e
    ) {
        log.warn(e.getMessage());
        requestLogging(request);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse.from("잘못된 권한입니다."));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
        HttpServletRequest request,
        AuthenticationException e
    ) {
        log.warn(e.getMessage());
        requestLogging(request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse.from("잘못된 인증정보입니다."));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleDataNotFoundException(
        HttpServletRequest request,
        DataNotFoundException e
    ) {
        log.warn(e.getMessage());
        requestLogging(request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.from("데이터를 찾을 수 없습니다."));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleDataNotFoundException(
        HttpServletRequest request,
        DuplicationException e
    ) {
        log.warn(e.getMessage());
        requestLogging(request);
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse.from("이미 존재하는 데이터입니다."));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleExternalServiceException(
        HttpServletRequest request,
        ExternalServiceException e
    ) {
        log.warn(e.getMessage());
        requestLogging(request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.from("외부 API 호출 과정에서 문제가 발생했습니다."));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleDateTimeParseException(
        HttpServletRequest request,
        DateTimeParseException e
    ) {
        log.warn(e.getMessage());
        requestLogging(request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.from("잘못된 날짜 형식입니다."));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(
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
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.from("서버에서 오류가 발생했습니다."));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest webRequest
    ) {
        HttpServletRequest request = ((ServletWebRequest) webRequest).getRequest();
        log.warn("검증과정에서 문제가 발생했습니다. uri: {} {}, ", request.getMethod(), request.getRequestURI(), ex);
        requestLogging(request);
        String errorMessages = ex.getBindingResult().getAllErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ErrorResponse.from(errorMessages));
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
