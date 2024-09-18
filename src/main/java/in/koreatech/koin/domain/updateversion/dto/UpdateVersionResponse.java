package in.koreatech.koin.domain.updateversion.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.updateversion.model.UpdateContent;
import in.koreatech.koin.domain.updateversion.model.UpdateVersion;
import in.koreatech.koin.domain.updateversion.model.UpdateVersionType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record UpdateVersionResponse(
    @Schema(description = "업데이트 버전 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "업데이트 버전 타입", example = "android", requiredMode = REQUIRED)
    UpdateVersionType type,

    @Schema(description = "업데이트 버전", example = "3.5.0", requiredMode = REQUIRED)
    String version,

    @Schema(description = "업데이트 제목", example = "코인의 새로운 기능 업데이트", requiredMode = REQUIRED)
    String title,

    @Schema(description = "업데이트 버전 내용", requiredMode = REQUIRED)
    List<InnerUpdateVersionBody> body,

    @Schema(description = "생성일", example = "2021-06-21 13:00:00", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "수정일", example = "2021-06-21", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDateTime updatedAt
) {

    public static UpdateVersionResponse from(UpdateVersion version) {
        return new UpdateVersionResponse(
            version.getId(),
            version.getType(),
            version.getVersion(),
            version.getTitle(),
            version.getContents().stream()
                .map(InnerUpdateVersionBody::from)
                .toList(),
            version.getCreatedAt(),
            version.getUpdatedAt()
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerUpdateVersionBody(
        @Schema(description = "업데이트 버전 소제목", example = "백그라운드 푸시 알림", requiredMode = REQUIRED)
        String bodyTitle,

        @Schema(description = "업데이트 버전 본문", example = "정확하고 빠른 알림을 위해...", requiredMode = REQUIRED)
        String bodyContent
    ) {

        public static InnerUpdateVersionBody from(UpdateContent content) {
            return new InnerUpdateVersionBody(
                content.getTitle(),
                content.getContent()
            );
        }
    }
}
