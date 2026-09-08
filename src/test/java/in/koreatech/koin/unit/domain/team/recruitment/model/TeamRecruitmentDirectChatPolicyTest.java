package in.koreatech.koin.unit.domain.team.recruitment.model;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.ACTIVE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.READ_ONLY;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.TEAM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.GENERAL;
import static org.assertj.core.api.Assertions.assertThat;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentDirectChatPolicy;
import in.koreatech.koin.unit.fixture.UserFixture;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TeamRecruitmentDirectChatPolicyTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 28);

    @Test
    void 모집_중인_ACCEPTED_지원서는_TEAM_방이_없어도_DIRECT를_열수있다() {
        assertThat(canOpen(ACCEPTED, false, recruitment(RECRUITING), null)).isTrue();
    }

    @Test
    void 마감일이_지난_RECRUITING_모집글은_ACTIVE_TEAM_방이_있어도_DIRECT를_열수없다() {
        TeamRecruitment recruitment = recruitment(RECRUITING, LocalDate.of(2026, 8, 27));

        assertThat(canOpen(ACCEPTED, false, recruitment, teamRoom(recruitment, ACTIVE))).isFalse();
    }

    @Test
    void 정원충족으로_마감된_ACCEPTED_지원서는_ACTIVE_TEAM_방이_있으면_DIRECT를_열수있다() {
        TeamRecruitment recruitment = recruitment(CLOSED);

        assertThat(canOpen(ACCEPTED, false, recruitment, teamRoom(recruitment, ACTIVE))).isTrue();
    }

    @Test
    void 수동_기한_마감된_ACCEPTED_지원서는_READ_ONLY_TEAM_방이면_DIRECT를_열수없다() {
        TeamRecruitment recruitment = recruitment(CLOSED);

        assertThat(canOpen(ACCEPTED, false, recruitment, teamRoom(recruitment, READ_ONLY))).isFalse();
    }

    @Test
    void 삭제된_모집글은_ACTIVE_TEAM_방이_남아도_신규_DIRECT를_열수없다() {
        TeamRecruitment deleted = recruitment(CLOSED);
        deleted.markDeleted(LocalDate.of(2026, 8, 28).atStartOfDay());

        assertThat(canOpen(ACCEPTED, false, deleted, teamRoom(deleted, ACTIVE))).isFalse();
    }

    @Test
    void 기존_DIRECT_방은_모집글_상태와_관계없이_열수있다() {
        TeamRecruitment recruitment = recruitment(CLOSED);

        assertThat(canOpen(ACCEPTED, true, recruitment, teamRoom(recruitment, READ_ONLY))).isTrue();
    }

    @Test
    void ACCEPTED가_아니면_모집_중이어도_DIRECT를_열수없다() {
        assertThat(canOpen(PENDING, false, recruitment(RECRUITING), null)).isFalse();
    }

    private boolean canOpen(
        TeamRecruitmentApplicationStatus applicationStatus,
        boolean hasExistingDirectChat,
        TeamRecruitment recruitment,
        TeamRecruitmentChatRoom teamChatRoom
    ) {
        return TeamRecruitmentDirectChatPolicy.canOpenDirectChat(
            applicationStatus, hasExistingDirectChat, recruitment, teamChatRoom, TODAY);
    }

    private TeamRecruitment recruitment(TeamRecruitmentStatus status) {
        return recruitment(status, LocalDate.of(2026, 8, 31));
    }

    private TeamRecruitment recruitment(TeamRecruitmentStatus status, LocalDate deadlineDate) {
        return TeamRecruitment.builder()
            .id(10)
            .author(UserFixture.id_설정_코인_유저(1))
            .title("팀원 모집")
            .activityStartDate(LocalDate.of(2026, 9, 1))
            .activityEndDate(LocalDate.of(2026, 9, 30))
            .deadlineDate(deadlineDate)
            .recruitmentType(GENERAL)
            .maxParticipants(5)
            .currentParticipants(0)
            .description("모집 내용")
            .status(status)
            .build();
    }

    private TeamRecruitmentChatRoom teamRoom(
        TeamRecruitment recruitment,
        TeamRecruitmentChatRoomStatus status
    ) {
        return TeamRecruitmentChatRoom.builder()
            .id(40)
            .recruitment(recruitment)
            .roomScopeKey("TEAM")
            .roomType(TEAM)
            .status(status)
            .build();
    }
}
