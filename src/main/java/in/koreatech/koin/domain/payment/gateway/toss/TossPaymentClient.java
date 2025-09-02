package in.koreatech.koin.domain.payment.gateway.toss;

import static in.koreatech.koin.global.code.ApiResponseCode.INTERNAL_SERVER_ERROR;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.payment.gateway.toss.dto.request.TossPaymentConfirmRequest;
import in.koreatech.koin.domain.payment.gateway.toss.dto.response.TossPaymentConfirmResponse;
import in.koreatech.koin.domain.payment.gateway.toss.exception.TossPaymentErrorCode;
import in.koreatech.koin.domain.payment.gateway.toss.exception.TossPaymentErrorResponse;
import in.koreatech.koin.domain.payment.gateway.toss.exception.TossPaymentException;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.global.exception.custom.KoinIllegalStateException;

@Component
public class TossPaymentClient {

    private static final String AUTH_PREFIX = "Basic ";

    private final WebClient webClient;
    private final String secretKey;
    private final ObjectMapper objectMapper;

    public TossPaymentClient(
        ObjectMapper objectMapper,
        @Value("${toss-payment.api-base-url}") String baseUrl,
        @Value("${toss-payment.secret-key}") String secretKey
    ) {
        this.secretKey = secretKey;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .defaultHeader(AUTHORIZATION, buildAuthorizationHeader())
            .build();
    }

    public TossPaymentConfirmResponse requestConfirm(String paymentKey, String orderId, Integer amount) {
        TossPaymentConfirmRequest request = new TossPaymentConfirmRequest(paymentKey, orderId, amount);

        try {
            return webClient.post()
                .uri("/confirm")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TossPaymentConfirmResponse.class)
                .block();
        } catch (WebClientResponseException e) {
            throw handleErrorResponse(e);
        } catch (Exception e) {
            throw CustomException.of(INTERNAL_SERVER_ERROR);
        }
    }

    private RuntimeException handleErrorResponse(WebClientResponseException e) {
        try {
            String rawBody = new String(e.getResponseBodyAsByteArray(), UTF_8);
            TossPaymentErrorResponse error = objectMapper.readValue(rawBody, TossPaymentErrorResponse.class);
            TossPaymentErrorCode tossPaymentErrorCode = TossPaymentErrorCode.fromCode(error.code());
            return TossPaymentException.of(tossPaymentErrorCode.getMessage(), tossPaymentErrorCode.getStatusCode(),
                tossPaymentErrorCode.getCode());
        } catch (Exception ex) {
            return new KoinIllegalStateException("서버 에러가 발생했습니다. 관리자에게 문의해주세요.");
        }
    }

    private String buildAuthorizationHeader() {
        String encoded = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(UTF_8));
        return AUTH_PREFIX + encoded;
    }
}
