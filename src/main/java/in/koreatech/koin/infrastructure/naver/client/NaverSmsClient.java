package in.koreatech.koin.infrastructure.naver.client;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.infrastructure.naver.dto.NaverSmsResponse;
import in.koreatech.koin.infrastructure.naver.dto.NaverSmsSendRequest;
import in.koreatech.koin.infrastructure.naver.exception.NaverSmsException;

@Component
public class NaverSmsClient {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String serviceKey;
    private final String sendNumber;
    private final String accessKey;
    private final String secretKey;

    public NaverSmsClient(
        RestTemplate restTemplate,
        @Value("${naver.sms.apiUrl}") String apiUrl,
        @Value("${naver.sms.serviceId}") String serviceKey,
        @Value("${naver.sms.fromNumber}") String fromNumber,
        @Value("${naver.accessKey}") String accessKey,
        @Value("${naver.secretKey}") String secretKey
    ) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.serviceKey = serviceKey;
        this.sendNumber = fromNumber;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public void sendMessage(String content, String targetPhoneNumber) {
        String path = String.format("/sms/v2/services/%s/messages", serviceKey);

        NaverSmsSendRequest request = new NaverSmsSendRequest(
            "SMS",
            sendNumber,
            "COMM",
            content,
            List.of(new NaverSmsSendRequest.InnerMessage(targetPhoneNumber))
        );

        String timeStamp = Long.toString(System.currentTimeMillis());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-ncp-apigw-timestamp", timeStamp);
        headers.set("x-ncp-iam-access-key", accessKey);
        headers.set("x-ncp-apigw-signature-v2", makeSignature(path, timeStamp));

        HttpEntity<NaverSmsSendRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<NaverSmsResponse> response = restTemplate.exchange(
            apiUrl + path,
            HttpMethod.POST,
            entity,
            NaverSmsResponse.class
        );

        NaverSmsResponse body = response.getBody();
        if (body == null || !"202".equals(body.statusCode())) {
            throw new NaverSmsException("문자 호출과정에서 문제가 발생했습니다.");
        }
    }

    /**
     * <a href="https://api-fin.ncloud-docs.com/docs/common-ncpapi">네이버 클라우드 API 문서 예시 코드</a>
     */
    private String makeSignature(String url, String time) {
        try {
            String space = " ";
            String newLine = "\n";
            String method = "POST";
            String message = method
                + space
                + url
                + newLine
                + time
                + newLine
                + accessKey;

            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            byte[] rawHmac = mac.doFinal(message.getBytes(UTF_8));

            return Base64.encodeBase64String(rawHmac);
        } catch (Exception e) {
            throw new KoinIllegalArgumentException("잘못된 입력 값입니다.");
        }
    }
}
