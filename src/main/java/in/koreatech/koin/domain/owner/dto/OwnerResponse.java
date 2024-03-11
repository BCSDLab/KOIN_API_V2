package in.koreatech.koin.domain.owner.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerAttachment;
import in.koreatech.koin.domain.shop.model.Shop;
import io.swagger.v3.oas.annotations.media.Schema;

public record OwnerResponse(
    @Schema(description = "이메일", example = "example@gmail.com")
    String email,

    @Schema(description = "이름", example = "홍길동")
    String name,

    @Schema(description = "사업자 등록 번호", example = "123-45-67890")
    String company_number,

    @Schema(description = "첨부 파일 목록")
    List<InnerAttachmentResponse> attachments,

    @Schema(description = "가게 목록")
    List<InnerShopResponse> shops
) {

    public static OwnerResponse of(Owner owner, List<OwnerAttachment> attachments, List<Shop> shops) {
        return new OwnerResponse(
            owner.getUser().getEmail(),
            owner.getUser().getName(),
            owner.getCompanyRegistrationNumber(),
            attachments.stream()
                .map(InnerAttachmentResponse::from)
                .toList(),
            shops.stream()
                .map(InnerShopResponse::from)
                .toList()
        );
    }

    @JsonNaming(SnakeCaseStrategy.class)
    private record InnerAttachmentResponse(
        @Schema(description = "첨부 파일 ID", example = "1")
        Long id,

        @Schema(description = "첨부 파일 URL", example = "https://static.koreatech.in/1.png")
        String fileUrl,

        @Schema(description = "첨부 파일 이름", example = "1.jpg")
        String fileName
    ) {

        public static InnerAttachmentResponse from(OwnerAttachment ownerAttachment) {
            return new InnerAttachmentResponse(
                ownerAttachment.getId(),
                ownerAttachment.getUrl(),
                ownerAttachment.getName()
            );
        }
    }

    @JsonNaming(SnakeCaseStrategy.class)
    private record InnerShopResponse(
        @Schema(description = "가게 ID", example = "1")
        Long id,

        @Schema(description = "가게 이름", example = "가게1")
        String name
    ) {

        public static InnerShopResponse from(Shop shop) {
            return new InnerShopResponse(
                shop.getId(),
                shop.getName()
            );
        }
    }
}
