package in.koreatech.koin.global.exception;

import java.time.format.DateTimeParseException;

import in.koreatech.koin.domain.version.exception.VersionException;
import in.koreatech.koin.global.auth.exception.AuthException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn(e.getMessage());
        return ResponseEntity.badRequest().body(ErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn(e.getMessage());
        return ResponseEntity.badRequest().body("잘못된 요청입니다.");
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException e) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse.from("잘못된 인증정보입니다."));
    }

    @ExceptionHandler
    public ResponseEntity<String> handleVersionException(VersionException e) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleDataNotFoundException(DataNotFoundException e) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.from("데이터를 찾을 수 없습니다."));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleDateTimeParseException(DateTimeParseException e) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.from("잘못된 날짜 형식입니다. 올바른 형식: yyMMdd"));
    }
}
