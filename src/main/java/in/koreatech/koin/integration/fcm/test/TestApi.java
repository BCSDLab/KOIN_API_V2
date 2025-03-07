package in.koreatech.koin.integration.fcm.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.integration.fcm.client.MobileAppPath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Test: 테스트", description = "테스트용 API")
@RequestMapping("/test")
public interface TestApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "테스트용 알림 발송")
    @GetMapping("/notification")
    ResponseEntity<Void> testSendMessage(
        @Parameter(description = "device token") @RequestParam String deviceToken,
        @Parameter(description = "알림 제목") @RequestParam String title,
        @Parameter(description = "알림 내용") @RequestParam String body,
        @Parameter(description = "이미지 url") @RequestParam String image,
        @Parameter(description = "app path", required = true)
        @RequestParam(defaultValue = "HOME") MobileAppPath mobileAppPath,
        @Parameter(description = "스킴 uri(ex: shop?id=1)") @RequestParam String url
    );

}
