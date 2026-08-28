package in.koreatech.koin.unit.domain.team.recruitment.model;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.DELETED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.unit.fixture.UserFixture;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TeamRecruitmentTest {

    @Test
    void 승인_인원만_전체_참여자_수에_반영되고_정원에_도달하면_모집을_닫을_수_있다() {
        TeamRecruitment recruitment = recruitment(1, 2);

        recruitment.increaseCurrentParticipants();

        assertThat(recruitment.getCurrentParticipants()).isEqualTo(2);
        assertThat(recruitment.isRecruiting()).isTrue();

        recruitment.close();

        assertThat(recruitment.getStatus()).isEqualTo(CLOSED);
        assertThat(recruitment.isRecruiting()).isFalse();
    }

    @Test
    void 삭제하면_DELETED와_삭제_시각을_함께_기록한다() {
        TeamRecruitment recruitment = recruitment(0, 5);
        LocalDateTime deletedAt = LocalDateTime.of(2026, 8, 28, 12, 30);

        recruitment.markDeleted(deletedAt);

        assertThat(recruitment.getStatus()).isEqualTo(DELETED);
        assertThat(recruitment.getDeletedAt()).isEqualTo(deletedAt);
        assertThat(recruitment.isDeleted()).isTrue();
    }

    @Test
    void 전체_모집_정원을_초과하여_승인할_수_없다() {
        TeamRecruitment recruitment = recruitment(2, 2);

        assertThatThrownBy(recruitment::increaseCurrentParticipants)
            .isInstanceOf(IllegalStateException.class);
        assertThat(recruitment.getCurrentParticipants()).isEqualTo(2);
    }

    @Test
    void 삭제_시각_없이_삭제_상태로_전이할_수_없다() {
        TeamRecruitment recruitment = recruitment(0, 2);

        assertThatThrownBy(() -> recruitment.markDeleted(null))
            .isInstanceOf(NullPointerException.class);
        assertThat(recruitment.getStatus()).isEqualTo(RECRUITING);
    }

    @Test
    void 삭제_상태와_삭제_시각이_일치하지_않는_모집글을_생성할_수_없다() {
        assertThatThrownBy(() -> TeamRecruitment.builder()
            .status(DELETED)
            .build())
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> TeamRecruitment.builder()
            .status(RECRUITING)
            .deletedAt(LocalDateTime.of(2026, 8, 28, 12, 30))
            .build())
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 삭제된_모집글을_다시_마감_상태로_바꿀_수_없다() {
        TeamRecruitment recruitment = recruitment(0, 2);
        recruitment.markDeleted(LocalDateTime.of(2026, 8, 28, 12, 30));

        assertThatThrownBy(recruitment::close)
            .isInstanceOf(IllegalStateException.class);
        assertThat(recruitment.getStatus()).isEqualTo(DELETED);
    }

    private TeamRecruitment recruitment(Integer currentParticipants, Integer maxParticipants) {
        return TeamRecruitment.builder()
            .id(1)
            .author(UserFixture.id_설정_코인_유저(1))
            .activityStartDate(LocalDate.of(2026, 9, 1))
            .activityEndDate(LocalDate.of(2026, 9, 30))
            .deadlineDate(LocalDate.of(2026, 8, 31))
            .maxParticipants(maxParticipants)
            .currentParticipants(currentParticipants)
            .status(RECRUITING)
            .description("모집 내용")
            .build();
    }
}
