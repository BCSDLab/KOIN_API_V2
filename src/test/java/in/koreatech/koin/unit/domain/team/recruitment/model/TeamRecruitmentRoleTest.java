package in.koreatech.koin.unit.domain.team.recruitment.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentRole;
import org.junit.jupiter.api.Test;

class TeamRecruitmentRoleTest {

    @Test
    void 역할_승인_인원이_정원에_도달하면_역할이_마감된다() {
        TeamRecruitmentRole role = TeamRecruitmentRole.builder()
            .id(1)
            .name("백엔드")
            .maxParticipants(2)
            .displayOrder(1)
            .build();

        assertThat(role.isClosed()).isFalse();

        role.increaseCurrentParticipants();
        assertThat(role.isClosed()).isFalse();

        role.increaseCurrentParticipants();
        assertThat(role.isClosed()).isTrue();
    }

    @Test
    void 역할_승인_인원은_감소할_수_있고_정원보다_작아지면_다시_열린다() {
        TeamRecruitmentRole role = TeamRecruitmentRole.builder()
            .name("백엔드")
            .maxParticipants(1)
            .currentParticipants(1)
            .displayOrder(1)
            .build();

        role.decreaseCurrentParticipants();

        assertThat(role.getCurrentParticipants()).isZero();
        assertThat(role.isClosed()).isFalse();
    }

    @Test
    void 역할_정원을_초과하여_승인할_수_없다() {
        TeamRecruitmentRole role = TeamRecruitmentRole.builder()
            .name("백엔드")
            .maxParticipants(1)
            .currentParticipants(1)
            .displayOrder(1)
            .build();

        assertThatThrownBy(role::increaseCurrentParticipants)
            .isInstanceOf(IllegalStateException.class);
        assertThat(role.getCurrentParticipants()).isOne();
    }

    @Test
    void 역할_정원을_현재_승인_인원보다_작게_수정할_수_없다() {
        TeamRecruitmentRole role = TeamRecruitmentRole.builder()
            .name("백엔드")
            .maxParticipants(2)
            .currentParticipants(2)
            .displayOrder(1)
            .build();

        assertThatThrownBy(() -> role.modify("백엔드", 1, 1))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(role.getMaxParticipants()).isEqualTo(2);
    }
}
