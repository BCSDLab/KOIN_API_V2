package in.koreatech.koin.infrastructure.s3.convertor;

import static in.koreatech.koin.infrastructure.s3.model.ImageUploadDomain.TEAM_RECRUITMENT;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ImageUploadDomainEnumConverterTest {

    private final ImageUploadDomainEnumConverter converter = new ImageUploadDomainEnumConverter();

    @Test
    void 팀원_모집_업로드_도메인을_변환한다() {
        var result = converter.convert("team_recruitment");

        assertThat(result).isEqualTo(TEAM_RECRUITMENT);
    }
}
