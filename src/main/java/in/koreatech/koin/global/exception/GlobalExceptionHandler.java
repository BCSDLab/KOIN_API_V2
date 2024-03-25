package in.koreatech.koin.global.exception;

import java.time.format.DateTimeParseException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn(e.getMessage());
        return ResponseEntity.badRequest().body(ErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.from("요청 파라미터가 잘못되었습니다."));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleAuthorException(AuthorizationException e) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse.from("잘못된 권한입니다."));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleAuththenException(AuthenticationException e) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse.from("잘못된 인증정보입니다."));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleDataNotFoundException(DataNotFoundException e) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.from("데이터를 찾을 수 없습니다."));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleDataNotFoundException(DuplicationException e) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse.from("이미 존재하는 데이터입니다."));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleDateTimeParseException(DateTimeParseException e) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.from("잘못된 날짜 형식입니다."));
    }
}
