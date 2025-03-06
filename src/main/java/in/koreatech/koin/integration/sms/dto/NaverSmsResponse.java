package in.koreatech.koin.integration.sms.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

public record NaverSmsResponse(
    String requestId,
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDateTime requestTime,
    String statusCode,
    String statusName
) {

}
