package in.koreatech.koin.domain.ownershop.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

public record OwnerShopsResponse(
    String email,
    String name,
    String company_number,
    List<InnerAttachmentResponse> attachments,
    List<InnerShopResponse> shops
) {

    @JsonNaming(SnakeCaseStrategy.class)
    private record InnerAttachmentResponse(
        Long id,
        String fileUrl,
        String fileName
    ) {

    }

    @JsonNaming(SnakeCaseStrategy.class)
    private record InnerShopResponse(
        Long id,
        String name
    ) {

    }
}
