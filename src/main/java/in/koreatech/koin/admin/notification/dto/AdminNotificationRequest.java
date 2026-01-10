package in.koreatech.koin.admin.notification.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.common.model.MobileAppPath;
import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminNotificationRequest(
    @Schema(description = "알림 구독 타입", example = "ARTICLE_KEYWORD", requiredMode = REQUIRED)
    @NotNull(message = "알림 구독 타입은 필수 입력사항입니다.")
    NotificationSubscribeType subscribeType,

    @Schema(description = "알림 제목", example = "공지사항이 등록됐어요!", requiredMode = REQUIRED)
    @NotEmpty(message = "알림 제목은 필수 입력사항입니다.")
    String title,

    @Schema(description = "알림 내용", example = "아카데미 공지가 등록되었습니다.", requiredMode = REQUIRED)
    @NotEmpty(message = "알림 내용은 필수 입력사항입니다.")
    String message,

    @Schema(description = "알림 이미지 url", example = "https://testimage.com", requiredMode = NOT_REQUIRED)
    String imageUrl,

    @Schema(description = "스킴 uri", example = "keyword?id=16797", requiredMode = NOT_REQUIRED)
    String schemaUrl,

    @Schema(description = "앱 path", example = "KEYWORD", requiredMode = REQUIRED)
    @NotNull(message = "앱 path은 필수 입력사항입니다.")
    MobileAppPath mobileAppPath
) {

}
