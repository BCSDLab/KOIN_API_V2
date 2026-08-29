package in.koreatech.koin.unit.domain.teamrecruitment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMemberRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMessageRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.teamrecruitment.dto.DirectChatRoomResponse;
import in.koreatech.koin.domain.teamrecruitment.service.TeamRecruitmentChatService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.unit.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class TeamRecruitmentChatServiceTest {

    private static final Integer USER_ID = 1;
    private static final Integer OTHER_USER_ID = 2;
    private static final Integer RECRUITMENT_ID = 10;
    private static final Integer CHAT_ROOM_ID = 20;
    private static final Integer APPLICATION_ID = 30;

    @Mock
    private TeamRecruitmentRepository recruitmentRepository;

    @Mock
    private TeamRecruitmentApplicationRepository applicationRepository;

    @Mock
    private TeamRecruitmentChatRoomRepository chatRoomRepository;

    @Mock
    private TeamRecruitmentChatMemberRepository memberRepository;

    @Mock
    private TeamRecruitmentChatMessageRepository messageRepository;

    @InjectMocks
    private TeamRecruitmentChatService chatService;

    @Test
    void 존재하지_않는_채팅방_조회시_404를_반환한다() {
        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getChatRoom(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void 채팅방이_다른_모집글_소속이면_404를_반환한다() {
        TeamRecruitmentChatRoom chatRoom = mock(TeamRecruitmentChatRoom.class);
        TeamRecruitment wrongRecruitment = mock(TeamRecruitment.class);
        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.of(chatRoom));
        when(chatRoom.getRecruitment()).thenReturn(wrongRecruitment);
        when(wrongRecruitment.getId()).thenReturn(99);

        assertThatThrownBy(() -> chatService.getChatRoom(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void 채팅방_멤버가_아니면_403을_반환한다() {
        TeamRecruitmentChatRoom chatRoom = mock(TeamRecruitmentChatRoom.class);
        TeamRecruitment recruitment = mock(TeamRecruitment.class);
        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.of(chatRoom));
        when(chatRoom.getRecruitment()).thenReturn(recruitment);
        when(recruitment.getId()).thenReturn(RECRUITMENT_ID);
        when(memberRepository.existsByChatRoom_IdAndUser_Id(CHAT_ROOM_ID, USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> chatService.getChatRoom(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void 지원서가_다른_모집글_소속이면_404를_반환한다() {
        TeamRecruitmentApplication application = mock(TeamRecruitmentApplication.class);
        TeamRecruitment wrongRecruitment = mock(TeamRecruitment.class);
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(recruitmentRepository.findById(RECRUITMENT_ID)).thenReturn(Optional.of(mock(TeamRecruitment.class)));
        when(application.getRecruitment()).thenReturn(wrongRecruitment);
        when(wrongRecruitment.getId()).thenReturn(99);

        assertThatThrownBy(() -> chatService.getOrCreateDirectChatRoom(USER_ID, RECRUITMENT_ID, APPLICATION_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void 모집글_작성자가_아니면_403을_반환한다() {
        User otherAuthor = UserFixture.id_설정_코인_유저(OTHER_USER_ID);
        TeamRecruitmentApplication application = mock(TeamRecruitmentApplication.class);
        TeamRecruitment recruitment = mock(TeamRecruitment.class);
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(recruitmentRepository.findById(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(application.getRecruitment()).thenReturn(recruitment);
        when(recruitment.getId()).thenReturn(RECRUITMENT_ID);
        when(recruitment.getAuthor()).thenReturn(otherAuthor);

        assertThatThrownBy(() -> chatService.getOrCreateDirectChatRoom(USER_ID, RECRUITMENT_ID, APPLICATION_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void 이미_존재하는_DIRECT_채팅방은_새로_생성하지_않고_기존_채팅방을_반환한다() {
        User author = UserFixture.id_설정_코인_유저(USER_ID);
        User counterpart = UserFixture.id_설정_코인_유저(OTHER_USER_ID);
        TeamRecruitmentApplication application = mock(TeamRecruitmentApplication.class);
        TeamRecruitment recruitment = mock(TeamRecruitment.class);
        TeamRecruitmentChatRoom existingRoom = mock(TeamRecruitmentChatRoom.class);

        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(recruitmentRepository.findById(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(application.getRecruitment()).thenReturn(recruitment);
        when(recruitment.getId()).thenReturn(RECRUITMENT_ID);
        when(recruitment.getAuthor()).thenReturn(author);
        when(application.getApplicant()).thenReturn(counterpart);
        when(chatRoomRepository.findByRecruitment_IdAndApplication_IdAndRoomType(
                RECRUITMENT_ID, APPLICATION_ID, TeamRecruitmentChatRoomType.DIRECT))
                .thenReturn(Optional.of(existingRoom));
        when(existingRoom.getId()).thenReturn(CHAT_ROOM_ID);
        when(existingRoom.getRoomType()).thenReturn(TeamRecruitmentChatRoomType.DIRECT);
        when(existingRoom.getStatus()).thenReturn(in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.ACTIVE);

        DirectChatRoomResponse response = chatService.getOrCreateDirectChatRoom(USER_ID, RECRUITMENT_ID, APPLICATION_ID);

        assertThat(response.chatRoomId()).isEqualTo(CHAT_ROOM_ID);
        assertThat(response.roomName()).isEqualTo(counterpart.getNickname());
    }

    @Test
    void READ_ONLY_채팅방에_메시지_전송시_409를_반환한다() {
        TeamRecruitmentChatRoom chatRoom = mock(TeamRecruitmentChatRoom.class);
        TeamRecruitmentChatMember member = mock(TeamRecruitmentChatMember.class);

        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.of(chatRoom));
        when(memberRepository.findByChatRoom_IdAndUser_Id(CHAT_ROOM_ID, USER_ID))
                .thenReturn(Optional.of(member));
        when(chatRoom.isActive()).thenReturn(false);

        assertThatThrownBy(() -> chatService.createMessage(
                USER_ID, CHAT_ROOM_ID,
                new in.koreatech.koin.domain.teamrecruitment.dto.CreateChatMessageRequest("안녕", false)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }
}
