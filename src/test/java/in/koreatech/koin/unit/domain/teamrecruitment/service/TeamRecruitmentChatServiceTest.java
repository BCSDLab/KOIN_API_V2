package in.koreatech.koin.unit.domain.teamrecruitment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.mockito.ArgumentCaptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMessage;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMemberRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMessageRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentNotificationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentOutboxEventRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.teamrecruitment.dto.CreateChatMessageRequest;
import in.koreatech.koin.domain.teamrecruitment.dto.DirectChatRoomCreationResult;
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

    @Mock
    private TeamRecruitmentNotificationRepository notificationRepository;

    @Mock
    private TeamRecruitmentOutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TeamRecruitmentChatService chatService;

    @Test
    void 존재하지_않는_채팅방_조회시_404를_반환한다() {
        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getChatRoom(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_CHAT_NOT_FOUND));
    }

    @Test
    void 채팅방이_다른_모집글_소속이면_404를_반환한다() {
        TeamRecruitmentChatRoom chatRoom = mock(TeamRecruitmentChatRoom.class);
        TeamRecruitment wrongRecruitment = mock(TeamRecruitment.class);
        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.of(chatRoom));
        when(chatRoom.getRecruitment()).thenReturn(wrongRecruitment);
        when(wrongRecruitment.getId()).thenReturn(99);

        assertThatThrownBy(() -> chatService.getChatRoom(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_CHAT_NOT_FOUND));
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
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_CHAT_FORBIDDEN));
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
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_APPLICATION_NOT_FOUND));
    }

    @Test
    void 지원서가_ACCEPTED_상태가_아니면_409를_반환한다() {
        User author = UserFixture.id_설정_코인_유저(USER_ID);
        TeamRecruitmentApplication application = mock(TeamRecruitmentApplication.class);
        TeamRecruitment recruitment = mock(TeamRecruitment.class);
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(recruitmentRepository.findById(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(application.getRecruitment()).thenReturn(recruitment);
        when(recruitment.getId()).thenReturn(RECRUITMENT_ID);
        when(recruitment.getAuthor()).thenReturn(author);
        when(application.getStatus()).thenReturn(TeamRecruitmentApplicationStatus.PENDING);

        assertThatThrownBy(() -> chatService.getOrCreateDirectChatRoom(USER_ID, RECRUITMENT_ID, APPLICATION_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_APPLICATION_NOT_ACCEPTED));
    }

    @Test
    void 모집글이_모집_중이_아니면_409를_반환한다() {
        User author = UserFixture.id_설정_코인_유저(USER_ID);
        TeamRecruitmentApplication application = mock(TeamRecruitmentApplication.class);
        TeamRecruitment recruitment = mock(TeamRecruitment.class);
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(recruitmentRepository.findById(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(application.getRecruitment()).thenReturn(recruitment);
        when(recruitment.getId()).thenReturn(RECRUITMENT_ID);
        when(recruitment.getAuthor()).thenReturn(author);
        when(application.getStatus()).thenReturn(TeamRecruitmentApplicationStatus.ACCEPTED);
        when(recruitment.isRecruiting()).thenReturn(false);

        assertThatThrownBy(() -> chatService.getOrCreateDirectChatRoom(USER_ID, RECRUITMENT_ID, APPLICATION_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_CLOSED));
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
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_FORBIDDEN));
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
        when(application.getStatus()).thenReturn(TeamRecruitmentApplicationStatus.ACCEPTED);
        when(recruitment.isRecruiting()).thenReturn(true);
        when(recruitment.getAuthor()).thenReturn(author);
        when(application.getApplicant()).thenReturn(counterpart);
        when(chatRoomRepository.findByRecruitment_IdAndApplication_IdAndRoomType(
                RECRUITMENT_ID, APPLICATION_ID, TeamRecruitmentChatRoomType.DIRECT))
                .thenReturn(Optional.of(existingRoom));
        when(existingRoom.getId()).thenReturn(CHAT_ROOM_ID);
        when(existingRoom.getRoomType()).thenReturn(TeamRecruitmentChatRoomType.DIRECT);
        when(existingRoom.getStatus()).thenReturn(TeamRecruitmentChatRoomStatus.ACTIVE);

        DirectChatRoomCreationResult result = chatService.getOrCreateDirectChatRoom(USER_ID, RECRUITMENT_ID, APPLICATION_ID);

        assertThat(result.isNew()).isFalse();
        assertThat(result.response().chatRoomId()).isEqualTo(CHAT_ROOM_ID);
        assertThat(result.response().roomName()).isEqualTo(counterpart.getNickname());
    }

    @Test
    void 메시지_조회시_afterMessageId와_beforeMessageId_동시_사용하면_400을_반환한다() {
        assertThatThrownBy(() -> chatService.getMessages(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID, 1, 5, 10))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.ILLEGAL_ARGUMENT));
    }

    @Test
    void 메시지_조회시_limit이_범위를_벗어나면_400을_반환한다() {
        assertThatThrownBy(() -> chatService.getMessages(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID, null, null, 0))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.ILLEGAL_ARGUMENT));

        assertThatThrownBy(() -> chatService.getMessages(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID, null, null, 201))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.ILLEGAL_ARGUMENT));
    }

    @Test
    void 메시지_조회시_afterMessageId가_1_미만이면_400을_반환한다() {
        assertThatThrownBy(() -> chatService.getMessages(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID, 0, null, 10))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.ILLEGAL_ARGUMENT));
    }

    @Test
    void 메시지_조회시_beforeMessageId가_1_미만이면_400을_반환한다() {
        assertThatThrownBy(() -> chatService.getMessages(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID, null, 0, 10))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.ILLEGAL_ARGUMENT));
    }

    @Test
    void 메시지_조회시_채팅방이_다른_모집글_소속이면_404를_반환한다() {
        TeamRecruitmentChatRoom chatRoom = mock(TeamRecruitmentChatRoom.class);
        TeamRecruitment wrongRecruitment = mock(TeamRecruitment.class);
        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.of(chatRoom));
        when(chatRoom.getRecruitment()).thenReturn(wrongRecruitment);
        when(wrongRecruitment.getId()).thenReturn(99);

        assertThatThrownBy(() -> chatService.getMessages(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID, null, null, 10))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_CHAT_NOT_FOUND));
    }

    @Test
    void 메시지_전송시_채팅방이_다른_모집글_소속이면_404를_반환한다() {
        TeamRecruitmentChatRoom chatRoom = mock(TeamRecruitmentChatRoom.class);
        TeamRecruitment wrongRecruitment = mock(TeamRecruitment.class);
        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.of(chatRoom));
        when(chatRoom.getRecruitment()).thenReturn(wrongRecruitment);
        when(wrongRecruitment.getId()).thenReturn(99);

        assertThatThrownBy(() -> chatService.createMessage(
                USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID,
                new CreateChatMessageRequest("안녕", false)))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_CHAT_NOT_FOUND));
    }

    @Test
    void 닉네임_미설정_사용자가_포함된_DIRECT_채팅방_조회시_nickname이_익명_사용자로_응답된다() {
        User author = UserFixture.id_설정_코인_유저(USER_ID);
        User anonymousCounterpart = UserFixture.닉네임_없는_코인_유저(OTHER_USER_ID);
        TeamRecruitmentApplication application = mock(TeamRecruitmentApplication.class);
        TeamRecruitment recruitment = mock(TeamRecruitment.class);
        TeamRecruitmentChatRoom existingRoom = mock(TeamRecruitmentChatRoom.class);

        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(recruitmentRepository.findById(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(application.getRecruitment()).thenReturn(recruitment);
        when(recruitment.getId()).thenReturn(RECRUITMENT_ID);
        when(application.getStatus()).thenReturn(TeamRecruitmentApplicationStatus.ACCEPTED);
        when(recruitment.isRecruiting()).thenReturn(true);
        when(recruitment.getAuthor()).thenReturn(author);
        when(application.getApplicant()).thenReturn(anonymousCounterpart);
        when(chatRoomRepository.findByRecruitment_IdAndApplication_IdAndRoomType(
                RECRUITMENT_ID, APPLICATION_ID, TeamRecruitmentChatRoomType.DIRECT))
                .thenReturn(Optional.of(existingRoom));
        when(existingRoom.getId()).thenReturn(CHAT_ROOM_ID);
        when(existingRoom.getRoomType()).thenReturn(TeamRecruitmentChatRoomType.DIRECT);
        when(existingRoom.getStatus()).thenReturn(TeamRecruitmentChatRoomStatus.ACTIVE);

        DirectChatRoomCreationResult result = chatService.getOrCreateDirectChatRoom(USER_ID, RECRUITMENT_ID, APPLICATION_ID);

        assertThat(result.response().roomName()).isEqualTo(anonymousCounterpart.getDisplayNickname());
        assertThat(result.response().counterpart().nickname()).isEqualTo(anonymousCounterpart.getDisplayNickname());
    }

    @Test
    void 닉네임_미설정_사용자의_메시지_전송시_senderNickname이_익명_사용자로_저장된다() {
        User anonymousSender = UserFixture.닉네임_없는_코인_유저(USER_ID);
        TeamRecruitmentChatRoom chatRoom = mock(TeamRecruitmentChatRoom.class);
        TeamRecruitment recruitment = mock(TeamRecruitment.class);
        TeamRecruitmentChatMember senderMember = mock(TeamRecruitmentChatMember.class);
        TeamRecruitmentChatMessage savedMessage = mock(TeamRecruitmentChatMessage.class);

        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.of(chatRoom));
        when(chatRoom.getRecruitment()).thenReturn(recruitment);
        when(recruitment.getId()).thenReturn(RECRUITMENT_ID);
        when(memberRepository.findByChatRoom_IdAndUser_Id(CHAT_ROOM_ID, USER_ID)).thenReturn(Optional.of(senderMember));
        when(chatRoom.isActive()).thenReturn(true);
        when(senderMember.getUser()).thenReturn(anonymousSender);
        when(messageRepository.save(any(TeamRecruitmentChatMessage.class))).thenReturn(savedMessage);
        when(savedMessage.getId()).thenReturn(100);
        when(memberRepository.findAllByChatRoom_Id(CHAT_ROOM_ID)).thenReturn(List.of());
        when(savedMessage.getContent()).thenReturn("안녕");
        when(savedMessage.getSender()).thenReturn(anonymousSender);
        when(savedMessage.getSenderNickname()).thenReturn(anonymousSender.getDisplayNickname());
        when(savedMessage.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(savedMessage.getIsImage()).thenReturn(false);

        ArgumentCaptor<TeamRecruitmentChatMessage> captor = ArgumentCaptor.forClass(TeamRecruitmentChatMessage.class);
        chatService.createMessage(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID, new CreateChatMessageRequest("안녕", false));
        verify(messageRepository).save(captor.capture());

        assertThat(captor.getValue().getSenderNickname()).isEqualTo(anonymousSender.getDisplayNickname());
    }

    @Test
    void READ_ONLY_채팅방에_메시지_전송시_409를_반환한다() {
        TeamRecruitmentChatRoom chatRoom = mock(TeamRecruitmentChatRoom.class);
        TeamRecruitmentChatMember member = mock(TeamRecruitmentChatMember.class);
        TeamRecruitment recruitment = mock(TeamRecruitment.class);

        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.of(chatRoom));
        when(chatRoom.getRecruitment()).thenReturn(recruitment);
        when(recruitment.getId()).thenReturn(RECRUITMENT_ID);
        when(memberRepository.findByChatRoom_IdAndUser_Id(CHAT_ROOM_ID, USER_ID))
                .thenReturn(Optional.of(member));
        when(chatRoom.isActive()).thenReturn(false);

        assertThatThrownBy(() -> chatService.createMessage(
                USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID,
                new CreateChatMessageRequest("안녕", false)))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_CHAT_READ_ONLY));
    }
}
