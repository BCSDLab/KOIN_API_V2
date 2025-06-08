package in.koreatech.koin.infrastructure.naver.dto;

import java.util.List;

/**
 * <a href="https://api.ncloud-docs.com/docs/ko/ai-application-service-sens-smsv2">네이버 클라우드 API 문서</a>
 * @param type (SMS | LMS | MMS)
 * @param from string
 * @param contentType (COMM | AD)
 * @param content string
 * @param messages list
 */
public record NaverSmsSendRequest(
    String type,
    String from,
    String contentType,
    String content,
    List<InnerMessage> messages
) {

    /**
     * @param to -를 제외한 숫자만 입력
     */
    public record InnerMessage(
        String to
    ) {

    }
}
