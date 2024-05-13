package in.koreatech.koin.admin.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerAttachment;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminOwnerResponse(
    @Schema(description = "이메일", example = "example@gmail.com", requiredMode = REQUIRED)
    String email,

    @Schema(description = "이름", example = "김철수", requiredMode = REQUIRED)
    String name,

    @Schema(description = "사업자 등록 번호", example = "012-34-56789", requiredMode = REQUIRED)
    String companyRegistrationNumber,

    @Schema(description = "첨부파일 url 목록", requiredMode = REQUIRED)
    List<String> attachmentsUrl,

    @Schema(description = "가게 id 목록", requiredMode = REQUIRED)
    List<Integer> shopsId
) {
    public static AdminOwnerResponse of(Owner owner, List<Integer> shopsId) {
        return new AdminOwnerResponse(
            owner.getUser().getEmail(),
            owner.getUser().getName(),
            owner.getCompanyRegistrationNumber(),
            owner.getAttachments()
                .stream()
                .map(OwnerAttachment::getUrl)
                .collect(Collectors.toList()),
            shopsId
        );
    }
}
