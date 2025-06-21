package in.koreatech.koin.domain.order.address.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AddressSearchRequest(

    @Schema(description = "검색할 주소 키워드", example = "병천면 충절로", requiredMode = REQUIRED)
    @NotBlank
    String keyword,

    @Schema(description = "현재 페이지 번호", example = "1")
    @NotNull
    @Min(value = 1)
    Integer currentPage,

    @Schema(description = "페이지당 결과 수", example = "10")
    @NotNull
    @Min(value = 1)
    Integer countPerPage
) {

}
