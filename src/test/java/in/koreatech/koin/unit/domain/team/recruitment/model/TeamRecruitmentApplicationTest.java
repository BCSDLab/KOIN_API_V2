package in.koreatech.koin.unit.domain.team.recruitment.model;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.REJECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentRole;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.unit.fixture.UserFixture;
import org.junit.jupiter.api.Test;

class TeamRecruitmentApplicationTest {

    @Test
    void 지원서_생성시_프로필_snapshot과_지원_내용을_그대로_보존하고_PENDING으로_시작한다() {
        User author = UserFixture.id_설정_코인_유저(1);
        User applicant = UserFixture.id_설정_코인_유저(2);
        TeamRecruitment recruitment = TeamRecruitment.builder()
            .id(10)
            .author(author)
            .maxParticipants(5)
            .description("모집 내용")
            .build();
        TeamRecruitmentRole role = TeamRecruitmentRole.builder()
            .id(20)
            .recruitment(recruitment)
            .name("백엔드")
            .maxParticipants(2)
            .displayOrder(1)
            .build();
        String profileSnapshot = "{\"nickname\":\"지원자\",\"department\":\"컴퓨터공학부\",\"student_year\":2023}";

        TeamRecruitmentApplication application = TeamRecruitmentApplication.builder()
            .id(30)
            .recruitment(recruitment)
            .applicant(applicant)
            .role(role)
            .motivation("지원 동기")
            .availability("월수금 20시 이후")
            .profileSnapshot(profileSnapshot)
            .build();

        assertThat(application.getRecruitment()).isSameAs(recruitment);
        assertThat(application.getApplicant()).isSameAs(applicant);
        assertThat(application.getRole()).isSameAs(role);
        assertThat(application.getMotivation()).isEqualTo("지원 동기");
        assertThat(application.getAvailability()).isEqualTo("월수금 20시 이후");
        assertThat(application.getProfileSnapshot()).isEqualTo(profileSnapshot);
        assertThat(application.getSnapshotVersion()).isOne();
        assertThat(application.getStatus()).isEqualTo(PENDING);
    }

    @Test
    void PENDING_지원서를_승인하면_ACCEPTED로_전이한다() {
        TeamRecruitmentApplication application = application("snapshot");

        application.accept();

        assertThat(application.getStatus()).isEqualTo(ACCEPTED);
        assertThat(application.getDecisionReason()).isNull();
    }

    @Test
    void 이미_처리된_지원서는_다른_상태로_다시_변경할_수_없다() {
        TeamRecruitmentApplication application = application("snapshot");
        application.reject("RECRUITMENT_CLOSED");

        assertThatThrownBy(application::accept)
            .isInstanceOf(IllegalStateException.class);
        assertThat(application.getStatus()).isEqualTo(REJECTED);
        assertThat(application.getDecisionReason()).isEqualTo("RECRUITMENT_CLOSED");
    }

    @Test
    void 지원서를_거절하면_REJECTED와_결정_사유를_기록한다() {
        TeamRecruitmentApplication application = application("snapshot");

        application.reject("RECRUITMENT_CLOSED");

        assertThat(application.getStatus()).isEqualTo(REJECTED);
        assertThat(application.getDecisionReason()).isEqualTo("RECRUITMENT_CLOSED");
    }

    private TeamRecruitmentApplication application(String profileSnapshot) {
        return TeamRecruitmentApplication.builder()
            .recruitment(TeamRecruitment.builder().id(10).author(UserFixture.id_설정_코인_유저(1)).build())
            .applicant(UserFixture.id_설정_코인_유저(2))
            .motivation("지원 동기")
            .availability("가능")
            .profileSnapshot(profileSnapshot)
            .build();
    }
}
