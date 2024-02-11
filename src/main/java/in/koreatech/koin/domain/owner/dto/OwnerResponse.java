package in.koreatech.koin.domain.owner.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.owner.domain.Owner;
import in.koreatech.koin.domain.owner.domain.OwnerAttachment;
import in.koreatech.koin.domain.shop.model.Shop;

public record OwnerResponse(
    String email,
    String name,
    String company_number,
    List<InnerAttachmentResponse> attachments,
    List<InnerShopResponse> shops
) {

    public static OwnerResponse from(Owner owner) {
        return new OwnerResponse(
            owner.getUser().getEmail(),
            owner.getUser().getName(),
            owner.getCompanyRegistrationNumber(),
            owner.getAttachments().stream()
                .map(InnerAttachmentResponse::from).toList(),
            owner.getShops().stream()
                .map(InnerShopResponse::from).toList()
            );
    }

    @JsonNaming(SnakeCaseStrategy.class)
    private record InnerAttachmentResponse(
        Long id,
        String fileUrl,
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
        Long id,
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
