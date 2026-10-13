package in.koreatech.koin.unit.domain.teamrecruitment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMessage;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfile;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentRole;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMemberRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMessageRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMessageRepository.ChatRoomUnreadCount;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentNotificationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentOutboxEventRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentProfileRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.teamrecruitment.dto.ChatRoomResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.CreateChatMessageRequest;
import in.koreatech.koin.domain.teamrecruitment.dto.DirectChatRoomCreationResult;
import in.koreatech.koin.domain.teamrecruitment.dto.TeamRecruitmentChatRoomListItemResponse;
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
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-08-28T03:00:00Z"), KST);

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
    private TeamRecruitmentProfileRepository profileRepository;

    @Mock
    private TeamRecruitmentNotificationRepository notificationRepository;

    @Mock
    private TeamRecruitmentOutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Clock clock;

    @InjectMocks
    private TeamRecruitmentChatService chatService;

    @BeforeEach
    void setUp() {
        lenient().when(clock.withZone(KST)).thenReturn(FIXED_CLOCK);
        List<TeamRecruitmentProfile> profiles = List.of(
                profile(USER_ID, "모집자프로필"), profile(OTHER_USER_ID, "지원자프로필"));
        lenient().when(profileRepository.findAllByUserIdIn(any())).thenAnswer(invocation -> {
            Collection<Integer> userIds = invocation.getArgument(0);
            return profiles.stream().filter(profile -> userIds.contains(profile.getUserId())).toList();
        });
    }

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
    void 내_채팅방_목록은_최근_메시지순으로_TEAM과_DIRECT_정보를_반환한다() {
        TeamRecruitment recruitment = mock(TeamRecruitment.class);
        TeamRecruitmentChatRoom teamRoom = mock(TeamRecruitmentChatRoom.class);
        TeamRecruitmentChatRoom directRoom = mock(TeamRecruitmentChatRoom.class);
        TeamRecruitmentChatMember currentTeamMember = mock(TeamRecruitmentChatMember.class);
        TeamRecruitmentChatMember currentDirectMember = mock(TeamRecruitmentChatMember.class);
        TeamRecruitmentChatMember counterpartMember = mock(TeamRecruitmentChatMember.class);
        TeamRecruitmentChatMessage teamMessage = mock(TeamRecruitmentChatMessage.class);
        TeamRecruitmentChatMessage directMessage = mock(TeamRecruitmentChatMessage.class);
        ChatRoomUnreadCount unreadCount = mock(ChatRoomUnreadCount.class);
        User currentUser = UserFixture.id_설정_코인_유저(USER_ID);
        User counterpart = UserFixture.id_설정_코인_유저(OTHER_USER_ID);
        LocalDateTime teamMessageAt = LocalDateTime.of(2026, 8, 28, 12, 0);
        LocalDateTime directMessageAt = teamMessageAt.plusMinutes(1);

        when(currentTeamMember.getChatRoom()).thenReturn(teamRoom);
        when(currentDirectMember.getChatRoom()).thenReturn(directRoom);
        when(currentDirectMember.getUser()).thenReturn(currentUser);
        when(teamRoom.getId()).thenReturn(20);
        when(directRoom.getId()).thenReturn(21);
        when(teamRoom.getRecruitment()).thenReturn(recruitment);
        when(directRoom.getRecruitment()).thenReturn(recruitment);
        when(recruitment.getId()).thenReturn(RECRUITMENT_ID);
        when(recruitment.getTitle()).thenReturn("팀원 모집");
        when(teamRoom.getRoomType()).thenReturn(TeamRecruitmentChatRoomType.TEAM);
        when(directRoom.getRoomType()).thenReturn(TeamRecruitmentChatRoomType.DIRECT);
        when(counterpartMember.getChatRoom()).thenReturn(directRoom);
        when(counterpartMember.getUser()).thenReturn(counterpart);
        when(teamMessage.getChatRoom()).thenReturn(teamRoom);
        when(teamMessage.getId()).thenReturn(100);
        when(teamMessage.getContent()).thenReturn("팀 메시지");
        when(teamMessage.getCreatedAt()).thenReturn(teamMessageAt);
        when(teamMessage.getIsImage()).thenReturn(false);
        when(directMessage.getChatRoom()).thenReturn(directRoom);
        when(directMessage.getId()).thenReturn(101);
        when(directMessage.getContent()).thenReturn("개인 메시지");
        when(directMessage.getCreatedAt()).thenReturn(directMessageAt);
        when(directMessage.getIsImage()).thenReturn(false);
        when(unreadCount.getChatRoomId()).thenReturn(20);
        when(unreadCount.getUnreadMessageCount()).thenReturn(2L);
        when(memberRepository.findAllByUserIdWithChatRoomAndRecruitment(USER_ID))
            .thenReturn(List.of(currentTeamMember, currentDirectMember));
        when(memberRepository.findAllWithUsersByChatRoomIds(List.of(21)))
            .thenReturn(List.of(currentDirectMember, counterpartMember));
        when(messageRepository.findLatestByChatRoomIds(List.of(20, 21)))
            .thenReturn(List.of(teamMessage, directMessage));
        when(messageRepository.countUnreadMessagesByUserId(USER_ID)).thenReturn(List.of(unreadCount));

        List<TeamRecruitmentChatRoomListItemResponse> responses = chatService.getChatRooms(USER_ID);

        assertThat(responses).extracting(TeamRecruitmentChatRoomListItemResponse::chatRoomId)
            .containsExactly(directRoom.getId(), teamRoom.getId());
        assertThat(responses.get(0))
            .extracting(
                TeamRecruitmentChatRoomListItemResponse::roomName,
                TeamRecruitmentChatRoomListItemResponse::counterpartId,
                TeamRecruitmentChatRoomListItemResponse::counterpartNickname,
                TeamRecruitmentChatRoomListItemResponse::lastMessageId,
                TeamRecruitmentChatRoomListItemResponse::unreadMessageCount
            )
            .containsExactly("지원자프로필", OTHER_USER_ID, "지원자프로필", 101, 0);
        assertThat(responses.get(1))
            .extracting(
                TeamRecruitmentChatRoomListItemResponse::roomName,
                TeamRecruitmentChatRoomListItemResponse::counterpartId,
                TeamRecruitmentChatRoomListItemResponse::counterpartNickname,
                TeamRecruitmentChatRoomListItemResponse::lastMessageId,
                TeamRecruitmentChatRoomListItemResponse::unreadMessageCount
            )
            .containsExactly("팀원 모집", null, null, 100, 2);
    }

    @Test
    void 내_채팅방이_없으면_빈_목록을_반환한다() {
        when(memberRepository.findAllByUserIdWithChatRoomAndRecruitment(USER_ID)).thenReturn(List.of());

        assertThat(chatService.getChatRooms(USER_ID)).isEmpty();

        verifyNoInteractions(messageRepository);
    }

    @Test
    void 코인_닉네임이_없어도_DIRECT_채팅방_목록은_모집_프로필_닉네임을_반환한다() {
        TeamRecruitment recruitment = mock(TeamRecruitment.class);
        TeamRecruitmentChatRoom directRoom = mock(TeamRecruitmentChatRoom.class);
        TeamRecruitmentChatMember currentMember = mock(TeamRecruitmentChatMember.class);
        TeamRecruitmentChatMember counterpartMember = mock(TeamRecruitmentChatMember.class);
        User currentUser = UserFixture.id_설정_코인_유저(USER_ID);
        User anonymousCounterpart = UserFixture.닉네임_없는_코인_유저(OTHER_USER_ID);

        when(currentMember.getChatRoom()).thenReturn(directRoom);
        when(currentMember.getUser()).thenReturn(currentUser);
        when(directRoom.getId()).thenReturn(21);
        when(directRoom.getRecruitment()).thenReturn(recruitment);
        when(directRoom.getRoomType()).thenReturn(TeamRecruitmentChatRoomType.DIRECT);
        when(counterpartMember.getChatRoom()).thenReturn(directRoom);
        when(counterpartMember.getUser()).thenReturn(anonymousCounterpart);
        when(recruitment.getId()).thenReturn(RECRUITMENT_ID);
        when(memberRepository.findAllByUserIdWithChatRoomAndRecruitment(USER_ID))
            .thenReturn(List.of(currentMember));
        when(memberRepository.findAllWithUsersByChatRoomIds(List.of(21)))
            .thenReturn(List.of(currentMember, counterpartMember));
        when(messageRepository.findLatestByChatRoomIds(List.of(21))).thenReturn(List.of());
        when(messageRepository.countUnreadMessagesByUserId(USER_ID)).thenReturn(List.of());

        List<TeamRecruitmentChatRoomListItemResponse> responses = chatService.getChatRooms(USER_ID);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).roomName()).isEqualTo("지원자프로필");
        assertThat(responses.get(0).counterpartNickname()).isEqualTo("지원자프로필");
        assertThat(responses.get(0).counterpartNickname()).isNotNull();
    }

    @Test
    void GENERAL_TEAM_채팅방에_작성자만_있으면_인원은_2명_중_1명이다() {
        ChatRoomResponse response = getChatRoom(
                TeamRecruitmentType.GENERAL,
                TeamRecruitmentChatRoomType.TEAM,
                1,
                1
        );

        assertThat(response)
                .extracting(ChatRoomResponse::memberCount, ChatRoomResponse::maxMemberCount)
                .containsExactly(1, 2);
    }

    @Test
    void ROLE_BASED_TEAM_채팅방에_마지막_지원자가_참여하면_인원은_2명_중_2명이다() {
        ChatRoomResponse response = getChatRoom(
                TeamRecruitmentType.ROLE_BASED,
                TeamRecruitmentChatRoomType.TEAM,
                1,
                2
        );

        assertThat(response)
                .extracting(ChatRoomResponse::memberCount, ChatRoomResponse::maxMemberCount)
                .containsExactly(2, 2);
    }

    @Test
    void DIRECT_채팅방_인원은_항상_2명_중_2명이다() {
        ChatRoomResponse response = getChatRoom(
                TeamRecruitmentType.GENERAL,
                TeamRecruitmentChatRoomType.DIRECT,
                1,
                2
        );

        assertThat(response)
                .extracting(ChatRoomResponse::memberCount, ChatRoomResponse::maxMemberCount)
                .containsExactly(2, 2);
    }

    @Test
    void 지원서가_다른_모집글_소속이면_404를_반환한다() {
        TeamRecruitmentApplication application = mock(TeamRecruitmentApplication.class);
        TeamRecruitment wrongRecruitment = mock(TeamRecruitment.class);
        when(recruitmentRepository.findByIdWithLock(RECRUITMENT_ID))
                .thenReturn(Optional.of(mock(TeamRecruitment.class)));
        when(applicationRepository.findByIdAndRecruitmentIdWithLock(APPLICATION_ID, RECRUITMENT_ID))
                .thenReturn(Optional.of(application));
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
        when(recruitmentRepository.findByIdWithLock(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(applicationRepository.findByIdAndRecruitmentIdWithLock(APPLICATION_ID, RECRUITMENT_ID))
                .thenReturn(Optional.of(application));
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
        when(recruitmentRepository.findByIdWithLock(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(applicationRepository.findByIdAndRecruitmentIdWithLock(APPLICATION_ID, RECRUITMENT_ID))
                .thenReturn(Optional.of(application));
        when(application.getRecruitment()).thenReturn(recruitment);
        when(recruitment.getId()).thenReturn(RECRUITMENT_ID);
        when(recruitment.getAuthor()).thenReturn(author);
        when(application.getStatus()).thenReturn(TeamRecruitmentApplicationStatus.ACCEPTED);
        when(application.getApplicant()).thenReturn(UserFixture.id_설정_코인_유저(OTHER_USER_ID));
        when(recruitment.getStatus()).thenReturn(TeamRecruitmentStatus.CLOSED);
        when(recruitment.isRecruiting()).thenReturn(false);

        assertThatThrownBy(() -> chatService.getOrCreateDirectChatRoom(USER_ID, RECRUITMENT_ID, APPLICATION_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_CLOSED));
    }

    @Test
    void 모집글_작성자나_해당_지원자가_아니면_403을_반환한다() {
        User otherAuthor = UserFixture.id_설정_코인_유저(OTHER_USER_ID);
        TeamRecruitmentApplication application = mock(TeamRecruitmentApplication.class);
        TeamRecruitment recruitment = mock(TeamRecruitment.class);
        when(recruitmentRepository.findByIdWithLock(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(applicationRepository.findByIdAndRecruitmentIdWithLock(APPLICATION_ID, RECRUITMENT_ID))
                .thenReturn(Optional.of(application));
        when(application.getRecruitment()).thenReturn(recruitment);
        when(recruitment.getId()).thenReturn(RECRUITMENT_ID);
        when(recruitment.getAuthor()).thenReturn(otherAuthor);
        when(application.getApplicant()).thenReturn(UserFixture.id_설정_코인_유저(3));

        assertThatThrownBy(() -> chatService.getOrCreateDirectChatRoom(USER_ID, RECRUITMENT_ID, APPLICATION_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_FORBIDDEN));
    }

    @Test
    void 승인된_지원자가_DIRECT를_생성하면_모집자가_상대방이고_양쪽이_멤버로_저장된다() {
        User author = UserFixture.id_설정_코인_유저(USER_ID);
        User applicant = UserFixture.id_설정_코인_유저(OTHER_USER_ID);
        TeamRecruitmentApplication application = mock(TeamRecruitmentApplication.class);
        TeamRecruitment recruitment = mock(TeamRecruitment.class);
        TeamRecruitmentChatRoom savedRoom = mock(TeamRecruitmentChatRoom.class);
        when(recruitmentRepository.findByIdWithLock(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(applicationRepository.findByIdAndRecruitmentIdWithLock(APPLICATION_ID, RECRUITMENT_ID))
                .thenReturn(Optional.of(application));
        when(application.getRecruitment()).thenReturn(recruitment);
        when(recruitment.getId()).thenReturn(RECRUITMENT_ID);
        when(recruitment.getAuthor()).thenReturn(author);
        when(application.getApplicant()).thenReturn(applicant);
        when(application.getStatus()).thenReturn(TeamRecruitmentApplicationStatus.ACCEPTED);
        when(recruitment.isRecruiting()).thenReturn(true);
        when(chatRoomRepository.save(any(TeamRecruitmentChatRoom.class))).thenReturn(savedRoom);
        when(savedRoom.getId()).thenReturn(CHAT_ROOM_ID);
        when(savedRoom.getRoomType()).thenReturn(TeamRecruitmentChatRoomType.DIRECT);

        DirectChatRoomCreationResult result = chatService.getOrCreateDirectChatRoom(
                OTHER_USER_ID, RECRUITMENT_ID, APPLICATION_ID);

        assertThat(result.isNew()).isTrue();
        assertThat(result.response().counterpart().id()).isEqualTo(USER_ID);
        assertThat(result.response().roomName()).isEqualTo("모집자프로필");
        assertThat(result.response().counterpart().nickname()).isEqualTo("모집자프로필");
        ArgumentCaptor<TeamRecruitmentChatMember> members = ArgumentCaptor.forClass(TeamRecruitmentChatMember.class);
        verify(memberRepository, times(2)).save(members.capture());
        assertThat(members.getAllValues())
                .extracting(member -> member.getUser().getId())
                .containsExactlyInAnyOrder(USER_ID, OTHER_USER_ID);
    }

    @Test
    void 승인된_지원자가_기존_DIRECT를_조회하면_모집자가_상대방이고_멤버를_추가하지_않는다() {
        User author = UserFixture.id_설정_코인_유저(USER_ID);
        User applicant = UserFixture.id_설정_코인_유저(OTHER_USER_ID);
        TeamRecruitmentApplication application = mock(TeamRecruitmentApplication.class);
        TeamRecruitment recruitment = mock(TeamRecruitment.class);
        TeamRecruitmentChatRoom existingRoom = mock(TeamRecruitmentChatRoom.class);
        when(recruitmentRepository.findByIdWithLock(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(applicationRepository.findByIdAndRecruitmentIdWithLock(APPLICATION_ID, RECRUITMENT_ID))
                .thenReturn(Optional.of(application));
        when(application.getRecruitment()).thenReturn(recruitment);
        when(recruitment.getId()).thenReturn(RECRUITMENT_ID);
        when(recruitment.getAuthor()).thenReturn(author);
        when(application.getApplicant()).thenReturn(applicant);
        when(application.getStatus()).thenReturn(TeamRecruitmentApplicationStatus.ACCEPTED);
        when(chatRoomRepository.findByRecruitment_IdAndApplication_IdAndRoomType(
                RECRUITMENT_ID, APPLICATION_ID, TeamRecruitmentChatRoomType.DIRECT))
                .thenReturn(Optional.of(existingRoom));
        when(existingRoom.getId()).thenReturn(CHAT_ROOM_ID);
        when(existingRoom.getRoomType()).thenReturn(TeamRecruitmentChatRoomType.DIRECT);

        DirectChatRoomCreationResult result = chatService.getOrCreateDirectChatRoom(
                OTHER_USER_ID, RECRUITMENT_ID, APPLICATION_ID);

        assertThat(result.isNew()).isFalse();
        assertThat(result.response().chatRoomId()).isEqualTo(CHAT_ROOM_ID);
        assertThat(result.response().counterpart().id()).isEqualTo(USER_ID);
        assertThat(result.response().roomName()).isEqualTo("모집자프로필");
        assertThat(result.response().counterpart().nickname()).isEqualTo("모집자프로필");
        verifyNoInteractions(memberRepository);
    }

    @Test
    void 이미_존재하는_DIRECT_채팅방은_새로_생성하지_않고_기존_채팅방을_반환한다() {
        User author = UserFixture.id_설정_코인_유저(USER_ID);
        User counterpart = UserFixture.id_설정_코인_유저(OTHER_USER_ID);
        TeamRecruitmentApplication application = mock(TeamRecruitmentApplication.class);
        TeamRecruitment recruitment = mock(TeamRecruitment.class);
        TeamRecruitmentChatRoom existingRoom = mock(TeamRecruitmentChatRoom.class);

        when(recruitmentRepository.findByIdWithLock(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(applicationRepository.findByIdAndRecruitmentIdWithLock(APPLICATION_ID, RECRUITMENT_ID))
                .thenReturn(Optional.of(application));
        when(application.getRecruitment()).thenReturn(recruitment);
        when(recruitment.getId()).thenReturn(RECRUITMENT_ID);
        when(application.getStatus()).thenReturn(TeamRecruitmentApplicationStatus.ACCEPTED);
        when(recruitment.getAuthor()).thenReturn(author);
        when(application.getApplicant()).thenReturn(counterpart);
        when(chatRoomRepository.findByRecruitment_IdAndApplication_IdAndRoomType(
                RECRUITMENT_ID, APPLICATION_ID, TeamRecruitmentChatRoomType.DIRECT))
                .thenReturn(Optional.of(existingRoom));
        when(existingRoom.getId()).thenReturn(CHAT_ROOM_ID);
        when(existingRoom.getRoomType()).thenReturn(TeamRecruitmentChatRoomType.DIRECT);

        DirectChatRoomCreationResult result = chatService.getOrCreateDirectChatRoom(USER_ID, RECRUITMENT_ID, APPLICATION_ID);

        assertThat(result.isNew()).isFalse();
        assertThat(result.response().chatRoomId()).isEqualTo(CHAT_ROOM_ID);
        assertThat(result.response().roomName()).isEqualTo("지원자프로필");

        InOrder lockOrder = inOrder(recruitmentRepository, applicationRepository, chatRoomRepository);
        lockOrder.verify(recruitmentRepository).findByIdWithLock(RECRUITMENT_ID);
        lockOrder.verify(applicationRepository)
                .findByIdAndRecruitmentIdWithLock(APPLICATION_ID, RECRUITMENT_ID);
        lockOrder.verify(chatRoomRepository).findByRecruitment_IdAndApplication_IdAndRoomType(
                RECRUITMENT_ID, APPLICATION_ID, TeamRecruitmentChatRoomType.DIRECT);
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
    void 코인_닉네임이_없어도_기존_DIRECT_채팅방은_모집_프로필_닉네임을_반환한다() {
        User author = UserFixture.id_설정_코인_유저(USER_ID);
        User anonymousCounterpart = UserFixture.닉네임_없는_코인_유저(OTHER_USER_ID);
        TeamRecruitmentApplication application = mock(TeamRecruitmentApplication.class);
        TeamRecruitment recruitment = mock(TeamRecruitment.class);
        TeamRecruitmentChatRoom existingRoom = mock(TeamRecruitmentChatRoom.class);

        when(recruitmentRepository.findByIdWithLock(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(applicationRepository.findByIdAndRecruitmentIdWithLock(APPLICATION_ID, RECRUITMENT_ID))
                .thenReturn(Optional.of(application));
        when(application.getRecruitment()).thenReturn(recruitment);
        when(recruitment.getId()).thenReturn(RECRUITMENT_ID);
        when(recruitment.getAuthor()).thenReturn(author);
        when(application.getStatus()).thenReturn(TeamRecruitmentApplicationStatus.ACCEPTED);
        when(application.getApplicant()).thenReturn(anonymousCounterpart);
        when(chatRoomRepository.findByRecruitment_IdAndApplication_IdAndRoomType(
                RECRUITMENT_ID, APPLICATION_ID, TeamRecruitmentChatRoomType.DIRECT))
                .thenReturn(Optional.of(existingRoom));
        when(existingRoom.getId()).thenReturn(CHAT_ROOM_ID);
        when(existingRoom.getRoomType()).thenReturn(TeamRecruitmentChatRoomType.DIRECT);

        DirectChatRoomCreationResult result = chatService.getOrCreateDirectChatRoom(USER_ID, RECRUITMENT_ID, APPLICATION_ID);

        assertThat(result.response().roomName()).isEqualTo("지원자프로필");
        assertThat(result.response().counterpart().nickname()).isEqualTo("지원자프로필");
    }

    @Test
    void 코인_닉네임이_없는_사용자의_메시지도_모집_프로필_닉네임으로_저장하고_응답한다() {
        User anonymousSender = UserFixture.닉네임_없는_코인_유저(USER_ID);
        TeamRecruitmentChatRoom chatRoom = mock(TeamRecruitmentChatRoom.class);
        TeamRecruitment recruitment = mock(TeamRecruitment.class);
        TeamRecruitmentChatMember senderMember = mock(TeamRecruitmentChatMember.class);

        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.of(chatRoom));
        when(chatRoom.getRecruitment()).thenReturn(recruitment);
        when(recruitment.getId()).thenReturn(RECRUITMENT_ID);
        when(memberRepository.findByChatRoom_IdAndUser_Id(CHAT_ROOM_ID, USER_ID)).thenReturn(Optional.of(senderMember));
        when(recruitment.getAuthor()).thenReturn(anonymousSender);
        when(senderMember.getUser()).thenReturn(anonymousSender);
        when(messageRepository.save(any(TeamRecruitmentChatMessage.class))).thenAnswer(invocation -> {
            TeamRecruitmentChatMessage message = invocation.getArgument(0);
            return TeamRecruitmentChatMessage.builder().id(100).chatRoom(chatRoom).sender(message.getSender())
                    .senderNickname(message.getSenderNickname()).content(message.getContent())
                    .isImage(message.getIsImage()).build();
        });
        when(memberRepository.findAllByChatRoom_Id(CHAT_ROOM_ID)).thenReturn(List.of());

        ArgumentCaptor<TeamRecruitmentChatMessage> captor = ArgumentCaptor.forClass(TeamRecruitmentChatMessage.class);
        var response = chatService.createMessage(
                USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID, new CreateChatMessageRequest("안녕", false));
        verify(messageRepository).save(captor.capture());

        assertThat(captor.getValue().getSenderNickname()).isEqualTo("모집자프로필");
        assertThat(response.userNickname()).isEqualTo("모집자프로필");
    }

    @ParameterizedTest
    @EnumSource(TeamRecruitmentChatRoomType.class)
    void 과거_메시지는_현재_프로필로_표시하고_멤버가_아닌_발신자도_중복없이_한번_조회한다(
            TeamRecruitmentChatRoomType roomType) {
        User author = UserFixture.id_설정_코인_유저(USER_ID);
        User applicant = UserFixture.id_설정_코인_유저(OTHER_USER_ID);
        stubMessages(roomType, List.of(
                message(100, author), message(101, applicant), message(102, applicant)));

        var responses = chatService.getMessages(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID, null, null, 10);

        assertThat(responses).extracting(response -> response.userNickname())
                .containsExactly("모집자프로필", "지원자프로필", "지원자프로필");
        verify(profileRepository).findAllByUserIdIn(List.of(USER_ID, OTHER_USER_ID));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void 프로필이_없거나_공백이면_공통닉과_저장된_발신자명_대신_익명_사용자로_표시한다(boolean missing) {
        doReturn(missing ? List.of() : List.of(profile(USER_ID, " "), profile(OTHER_USER_ID, "\t")))
                .when(profileRepository).findAllByUserIdIn(any());
        stubMessages(TeamRecruitmentChatRoomType.TEAM, List.of(
                message(100, UserFixture.id_설정_코인_유저(USER_ID)),
                message(101, UserFixture.닉네임_없는_코인_유저(OTHER_USER_ID))));

        var responses = chatService.getMessages(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID, null, null, 10);
        var directRoom = getChatRoom(TeamRecruitmentType.GENERAL, TeamRecruitmentChatRoomType.DIRECT, 1, 2);

        assertThat(responses).extracting(response -> response.userNickname())
                .containsExactly("익명 사용자", "익명 사용자");
        assertThat(directRoom.roomName()).isEqualTo("익명 사용자");
        assertThat(directRoom.counterpart().nickname()).isEqualTo("익명 사용자");
    }

    @Test
    void 프로필_닉네임을_변경하면_이미_저장된_메시지도_변경된_닉네임으로_응답한다() {
        TeamRecruitmentProfile senderProfile = profile(OTHER_USER_ID, "변경전프로필");
        doReturn(List.of(senderProfile)).when(profileRepository).findAllByUserIdIn(any());
        TeamRecruitmentChatMessage message = message(100, UserFixture.id_설정_코인_유저(OTHER_USER_ID));
        stubMessages(TeamRecruitmentChatRoomType.TEAM, List.of(message));

        var before = chatService.getMessages(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID, null, null, 10);
        senderProfile.replace("변경후프로필", "백엔드", "자기소개");
        var after = chatService.getMessages(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID, null, null, 10);

        assertThat(before.get(0).userNickname()).isEqualTo("변경전프로필");
        assertThat(after.get(0).userNickname()).isEqualTo("변경후프로필");
        assertThat(message.getSenderNickname()).isEqualTo("저장된 과거 닉네임");
        verify(profileRepository, times(2)).findAllByUserIdIn(any());
    }

    @Test
    void 탈퇴자의_메시지를_표시하고_남은_멤버의_읽음_상태를_계산한다() {
        TeamRecruitment recruitment = TeamRecruitment.builder().id(RECRUITMENT_ID)
                .author(null).status(TeamRecruitmentStatus.CLOSED).build();
        TeamRecruitmentChatRoom room = TeamRecruitmentChatRoom.builder()
                .id(CHAT_ROOM_ID).recruitment(recruitment).roomType(TeamRecruitmentChatRoomType.TEAM)
                .status(TeamRecruitmentChatRoomStatus.ACTIVE).build();
        TeamRecruitmentChatMember reader = TeamRecruitmentChatMember.builder()
                .chatRoom(room).user(UserFixture.id_설정_코인_유저(USER_ID)).build();
        TeamRecruitmentChatMember unreadMember = TeamRecruitmentChatMember.builder()
                .chatRoom(room).user(UserFixture.id_설정_코인_유저(OTHER_USER_ID)).build();
        TeamRecruitmentChatMessage message = TeamRecruitmentChatMessage.builder()
                .id(100).chatRoom(room).sender(null).senderNickname("기존 닉네임")
                .content("보존된 메시지").isImage(true).build();
        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.of(room));
        when(memberRepository.findByChatRoom_IdAndUser_Id(CHAT_ROOM_ID, USER_ID)).thenReturn(Optional.of(reader));
        when(messageRepository.findAllByChatRoom_IdOrderByIdAsc(
                org.mockito.ArgumentMatchers.eq(CHAT_ROOM_ID), any())).thenReturn(List.of(message));
        when(memberRepository.findAllByChatRoom_Id(CHAT_ROOM_ID)).thenReturn(List.of(reader, unreadMember));

        var responses = chatService.getMessages(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID, null, null, 10);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).userId()).isNull();
        assertThat(responses.get(0).userNickname()).isEqualTo("탈퇴한 회원");
        assertThat(responses.get(0).content()).isEqualTo("보존된 메시지");
        assertThat(responses.get(0).isImage()).isTrue();
        assertThat(responses.get(0).unreadCount()).isEqualTo(1);
        assertThat(reader.getLastReadMessageId()).isEqualTo(100);
    }

    @Test
    void 상대방이_탈퇴한_DIRECT_목록과_상세는_탈퇴한_회원으로_표시한다() {
        TeamRecruitment recruitment = TeamRecruitment.builder().id(RECRUITMENT_ID).title("모집 제목").build();
        TeamRecruitmentChatRoom room = TeamRecruitmentChatRoom.builder().id(CHAT_ROOM_ID)
                .recruitment(recruitment).roomType(TeamRecruitmentChatRoomType.DIRECT).build();
        TeamRecruitmentChatMember member = TeamRecruitmentChatMember.builder()
                .chatRoom(room).user(UserFixture.id_설정_코인_유저(USER_ID)).build();
        when(memberRepository.findAllByUserIdWithChatRoomAndRecruitment(USER_ID)).thenReturn(List.of(member));
        when(memberRepository.findAllWithUsersByChatRoomIds(List.of(CHAT_ROOM_ID))).thenReturn(List.of(member));
        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.of(room));
        when(memberRepository.existsByChatRoom_IdAndUser_Id(CHAT_ROOM_ID, USER_ID)).thenReturn(true);
        when(memberRepository.countByChatRoom_Id(CHAT_ROOM_ID)).thenReturn(1L);
        when(memberRepository.findAllByChatRoom_Id(CHAT_ROOM_ID)).thenReturn(List.of(member));

        var item = chatService.getChatRooms(USER_ID).get(0);
        var detail = chatService.getChatRoom(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID);

        assertThat(item.roomName()).isEqualTo("탈퇴한 회원");
        assertThat(item.counterpartId()).isNull();
        assertThat(item.counterpartNickname()).isEqualTo("탈퇴한 회원");
        assertThat(detail.roomName()).isEqualTo("탈퇴한 회원");
        assertThat(detail.counterpart().id()).isNull();
        assertThat(detail.counterpart().nickname()).isEqualTo("탈퇴한 회원");
        assertThat(detail.memberCount()).isEqualTo(1);
        assertThat(detail.maxMemberCount()).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void 상대가_탈퇴해도_남은_사용자에게_기존_DIRECT_채팅방은_반환한다(boolean authorWithdrawn) {
        User survivor = UserFixture.id_설정_코인_유저(USER_ID);
        TeamRecruitmentApplication application = stubWithdrawnApplication(authorWithdrawn ? null : survivor);
        if (authorWithdrawn) {
            org.springframework.test.util.ReflectionTestUtils.setField(application, "applicant", survivor);
        }
        TeamRecruitmentChatRoom room = TeamRecruitmentChatRoom.builder().id(CHAT_ROOM_ID)
                .recruitment(application.getRecruitment()).application(application)
                .roomType(TeamRecruitmentChatRoomType.DIRECT).build();
        when(chatRoomRepository.findByRecruitment_IdAndApplication_IdAndRoomType(
                RECRUITMENT_ID, APPLICATION_ID, TeamRecruitmentChatRoomType.DIRECT)).thenReturn(Optional.of(room));

        var result = chatService.getOrCreateDirectChatRoom(USER_ID, RECRUITMENT_ID, APPLICATION_ID);

        assertThat(result.isNew()).isFalse();
        assertThat(result.response().roomName()).isEqualTo("탈퇴한 회원");
        assertThat(result.response().counterpart().id()).isNull();
        assertThat(result.response().counterpart().nickname()).isEqualTo("탈퇴한 회원");
        verifyNoInteractions(memberRepository);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void 상대가_탈퇴하면_새_DIRECT_채팅방은_생성하지_않는다(boolean authorWithdrawn) {
        User survivor = UserFixture.id_설정_코인_유저(USER_ID);
        TeamRecruitmentApplication application = stubWithdrawnApplication(authorWithdrawn ? null : survivor);
        if (authorWithdrawn) {
            org.springframework.test.util.ReflectionTestUtils.setField(application, "applicant", survivor);
        }

        assertThatThrownBy(() -> chatService.getOrCreateDirectChatRoom(USER_ID, RECRUITMENT_ID, APPLICATION_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_FORBIDDEN));
        verify(chatRoomRepository, org.mockito.Mockito.never()).save(any());
        verifyNoInteractions(memberRepository);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void 상대가_탈퇴한_DIRECT_채팅방은_메시지를_전송할_수_없다(boolean authorWithdrawn) {
        User survivor = UserFixture.id_설정_코인_유저(USER_ID);
        TeamRecruitment recruitment = TeamRecruitment.builder().id(RECRUITMENT_ID)
                .author(authorWithdrawn ? null : survivor).build();
        TeamRecruitmentApplication application = TeamRecruitmentApplication.builder()
                .recruitment(recruitment).applicant(authorWithdrawn ? survivor : null).build();
        TeamRecruitmentChatRoom room = TeamRecruitmentChatRoom.builder().id(CHAT_ROOM_ID)
                .recruitment(recruitment).application(application).roomType(TeamRecruitmentChatRoomType.DIRECT)
                .status(TeamRecruitmentChatRoomStatus.ACTIVE).build();
        TeamRecruitmentChatMember member = TeamRecruitmentChatMember.builder()
                .chatRoom(room).user(UserFixture.id_설정_코인_유저(USER_ID)).build();
        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.of(room));
        when(memberRepository.findByChatRoom_IdAndUser_Id(CHAT_ROOM_ID, USER_ID)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> chatService.createMessage(
                USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID, new CreateChatMessageRequest("전송 금지", false)))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_CHAT_FORBIDDEN));
        verifyNoInteractions(messageRepository, notificationRepository, outboxEventRepository);
        assertThat(member.getLastReadMessageId()).isNull();
    }

    @Test
    void 작성자가_탈퇴한_TEAM은_메시지_전송을_거절한다() {
        TeamRecruitment recruitment = TeamRecruitment.builder().id(RECRUITMENT_ID)
                .author(null).status(TeamRecruitmentStatus.CLOSED).build();
        TeamRecruitmentChatRoom room = TeamRecruitmentChatRoom.builder().id(CHAT_ROOM_ID)
                .recruitment(recruitment).roomType(TeamRecruitmentChatRoomType.TEAM)
                .status(TeamRecruitmentChatRoomStatus.ACTIVE).build();
        TeamRecruitmentChatMember member = TeamRecruitmentChatMember.builder()
                .chatRoom(room).user(UserFixture.id_설정_코인_유저(USER_ID)).build();
        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.of(room));
        when(memberRepository.findByChatRoom_IdAndUser_Id(CHAT_ROOM_ID, USER_ID)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> chatService.createMessage(
                USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID, new CreateChatMessageRequest("전송 금지", false)))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_CHAT_FORBIDDEN));
        verifyNoInteractions(messageRepository, notificationRepository, outboxEventRepository, objectMapper);
        assertThat(member.getLastReadMessageId()).isNull();
    }

    @Test
    void 일반_팀원_탈퇴_후에도_작성자가_있으면_남은_TEAM_멤버는_메시지를_전송한다() {
        User author = UserFixture.id_설정_코인_유저(OTHER_USER_ID);
        User sender = UserFixture.id_설정_코인_유저(USER_ID);
        TeamRecruitment recruitment = TeamRecruitment.builder().id(RECRUITMENT_ID)
                .author(author).status(TeamRecruitmentStatus.CLOSED).currentParticipants(1).build();
        TeamRecruitmentChatRoom room = TeamRecruitmentChatRoom.builder().id(CHAT_ROOM_ID)
                .recruitment(recruitment).roomType(TeamRecruitmentChatRoomType.TEAM).build();
        TeamRecruitmentChatMember senderMember = TeamRecruitmentChatMember.builder()
                .chatRoom(room).user(sender).build();
        TeamRecruitmentChatMember authorMember = TeamRecruitmentChatMember.builder()
                .chatRoom(room).user(author).build();
        // 탈퇴한 일반 팀원의 멤버 행은 삭제되고 작성자와 다른 팀원만 남은 상태다.
        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.of(room));
        when(memberRepository.findByChatRoom_IdAndUser_Id(CHAT_ROOM_ID, USER_ID))
                .thenReturn(Optional.of(senderMember));
        when(memberRepository.findAllByChatRoom_Id(CHAT_ROOM_ID)).thenReturn(List.of(senderMember, authorMember));
        when(messageRepository.save(any(TeamRecruitmentChatMessage.class))).thenAnswer(invocation -> {
            TeamRecruitmentChatMessage message = invocation.getArgument(0);
            return TeamRecruitmentChatMessage.builder().id(100).chatRoom(room).sender(message.getSender())
                    .senderNickname(message.getSenderNickname()).content(message.getContent())
                    .isImage(message.getIsImage()).build();
        });
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = chatService.createMessage(
                USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID, new CreateChatMessageRequest("계속 대화", false));

        assertThat(response.content()).isEqualTo("계속 대화");
        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.userNickname()).isEqualTo("모집자프로필");
        assertThat(response.unreadCount()).isEqualTo(1);
        assertThat(senderMember.getLastReadMessageId()).isEqualTo(100);
        verify(messageRepository).save(any(TeamRecruitmentChatMessage.class));
        verify(notificationRepository).save(any());
        verify(outboxEventRepository).save(any());
    }

    private TeamRecruitmentProfile profile(Integer userId, String nickname) {
        return TeamRecruitmentProfile.builder().userId(userId)
                .user(UserFixture.id_설정_코인_유저(userId)).profileNickname(nickname)
                .preferredRole("백엔드").selfIntroduction("자기소개").build();
    }

    private TeamRecruitmentChatMessage message(Integer id, User sender) {
        return TeamRecruitmentChatMessage.builder().id(id).sender(sender)
                .senderNickname("저장된 과거 닉네임").content("과거 메시지").isImage(false).build();
    }

    private void stubMessages(TeamRecruitmentChatRoomType roomType, List<TeamRecruitmentChatMessage> messages) {
        TeamRecruitment recruitment = TeamRecruitment.builder().id(RECRUITMENT_ID)
                .author(UserFixture.id_설정_코인_유저(USER_ID)).build();
        TeamRecruitmentChatRoom room = TeamRecruitmentChatRoom.builder().id(CHAT_ROOM_ID)
                .recruitment(recruitment).roomType(roomType).build();
        TeamRecruitmentChatMember reader = TeamRecruitmentChatMember.builder().chatRoom(room)
                .user(UserFixture.id_설정_코인_유저(USER_ID)).build();
        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.of(room));
        when(memberRepository.findByChatRoom_IdAndUser_Id(CHAT_ROOM_ID, USER_ID)).thenReturn(Optional.of(reader));
        when(messageRepository.findAllByChatRoom_IdOrderByIdAsc(
                org.mockito.ArgumentMatchers.eq(CHAT_ROOM_ID), any())).thenReturn(messages);
        when(memberRepository.findAllByChatRoom_Id(CHAT_ROOM_ID)).thenReturn(List.of(reader));
    }

    private TeamRecruitmentApplication stubWithdrawnApplication(User author) {
        TeamRecruitment recruitment = TeamRecruitment.builder().id(RECRUITMENT_ID).author(author).build();
        TeamRecruitmentApplication application = TeamRecruitmentApplication.builder().id(APPLICATION_ID)
                .recruitment(recruitment).applicant(null).status(TeamRecruitmentApplicationStatus.ACCEPTED).build();
        when(recruitmentRepository.findByIdWithLock(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(applicationRepository.findByIdAndRecruitmentIdWithLock(APPLICATION_ID, RECRUITMENT_ID))
                .thenReturn(Optional.of(application));
        return application;
    }

    private ChatRoomResponse getChatRoom(
            TeamRecruitmentType recruitmentType,
            TeamRecruitmentChatRoomType roomType,
            int maxParticipants,
            long memberCount
    ) {
        User currentUser = UserFixture.id_설정_코인_유저(USER_ID);
        User counterpart = UserFixture.id_설정_코인_유저(OTHER_USER_ID);
        int approvedParticipants = Math.toIntExact(memberCount) - 1;
        TeamRecruitment recruitment = TeamRecruitment.builder()
                .id(RECRUITMENT_ID)
                .author(currentUser)
                .title("팀원 모집")
                .recruitmentType(recruitmentType)
                .maxParticipants(maxParticipants)
                .currentParticipants(approvedParticipants)
                .build();
        if (recruitmentType == TeamRecruitmentType.ROLE_BASED) {
            recruitment.addRole(TeamRecruitmentRole.builder()
                    .name("백엔드")
                    .maxParticipants(maxParticipants)
                    .currentParticipants(approvedParticipants)
                    .displayOrder(1)
                    .build());
        }
        TeamRecruitmentChatRoom chatRoom = TeamRecruitmentChatRoom.builder()
                .id(CHAT_ROOM_ID)
                .recruitment(recruitment)
                .roomScopeKey(roomType.name())
                .roomType(roomType)
                .build();

        when(chatRoomRepository.findById(CHAT_ROOM_ID)).thenReturn(Optional.of(chatRoom));
        when(memberRepository.existsByChatRoom_IdAndUser_Id(CHAT_ROOM_ID, USER_ID)).thenReturn(true);
        when(memberRepository.countByChatRoom_Id(CHAT_ROOM_ID)).thenReturn(memberCount);
        if (roomType == TeamRecruitmentChatRoomType.DIRECT) {
            when(memberRepository.findAllByChatRoom_Id(CHAT_ROOM_ID)).thenReturn(List.of(
                    TeamRecruitmentChatMember.builder().chatRoom(chatRoom).user(currentUser).build(),
                    TeamRecruitmentChatMember.builder().chatRoom(chatRoom).user(counterpart).build()
            ));
        }

        return chatService.getChatRoom(USER_ID, RECRUITMENT_ID, CHAT_ROOM_ID);
    }
}
