package in.koreatech.koin.admin.version.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionContent;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminVersionUpdateRequest(
    @Schema(description = "업데이트 버전", example = "3.5.0", requiredMode = REQUIRED)
    String version,

    @Schema(description = "업데이트 제목", example = "코인의 새로운 기능 업데이트", requiredMode = REQUIRED)
    String title,

    @Schema(description = "업데이트 버전 내용", requiredMode = REQUIRED)
    List<InnerAdminVersionBody> body
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerAdminVersionBody(
        @Schema(description = "업데이트 버전 소제목", example = "백그라운드 푸시 알림", requiredMode = REQUIRED)
        String bodyTitle,

        @Schema(description = "업데이트 버전 본문", example = "정확하고 빠른 알림을 위해...", requiredMode = REQUIRED)
        String bodyContent
    ) {

        public VersionContent toEntity(Version version) {
            return VersionContent.builder()
                .version(version)
                .title(bodyTitle)
                .content(bodyContent)
                .build();
        }
    }

}
