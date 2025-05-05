package in.koreatech.koin.infrastructure.s3.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record UploadUrlResponse(
    @Schema(description = "파일을 업로드할 수 있는 url",
        example = """
            https://bucketname.ap-northeast-2.amazonaws.com/upload/domain/2000/00/00/d4cb13df-cf57-4612-b37d-80ecfa3f4621-1694847132589/image.jpg
                    ?x-amz-acl=public-read
                    &X-Amz-Algorithm=AWS4-HMAC-SHA256
                    &X-Amz-Date=20000000T000000Z
                    &X-Amz-SignedHeaders=content-length%3Bcontent-type%3Bhost
                    &X-Amz-Expires=7199&X-Amz-Credential=AKIA6BRP3Q6L5PUD5W5Q%2F20230916%2Fap-northeast-2%2Fs3%2Faws4_request
                    &X-Amz-Signature=796esadfsadfxcv213f851431a88bc16c8db048f322b8993e21e4829c531
            """)
    String preSignedUrl,

    @Schema(description = "첨부 파일 URL", example = "https://static.koreatech.in/1.png")
    String fileUrl,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "presigned url 만료 일자", example = "2023-01-01 12:34:56")
    LocalDateTime expirationDate
) {

}
