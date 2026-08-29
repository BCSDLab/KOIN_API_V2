package in.koreatech.koin.unit.domain.team.recruitment.dto;

import static org.assertj.core.api.Assertions.assertThat;

import in.koreatech.koin.domain.team.recruitment.dto.UpdateApplicationStatusRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import org.junit.jupiter.api.Test;

class TeamRecruitmentApplicationDtoContractTest {

    @Test
    void 지원_상태_변경_요청은_승인과_거절만_안내한다() throws NoSuchFieldException {
        Schema schema = UpdateApplicationStatusRequest.class
            .getDeclaredField("status")
            .getAnnotation(Schema.class);

        assertThat(schema.allowableValues()).containsExactly("ACCEPTED", "REJECTED");
    }
}
