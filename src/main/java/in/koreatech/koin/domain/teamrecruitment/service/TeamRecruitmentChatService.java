package in.koreatech.koin.domain.teamrecruitment.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMessage;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentDirectChatPolicy;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentOutboxEvent;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMemberRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMessageRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentNotificationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentOutboxEventRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.teamrecruitment.dto.ChatMessageResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.ChatRoomResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.CreateChatMessageRequest;
import in.koreatech.koin.domain.teamrecruitment.dto.DirectChatRoomCreationResult;
import in.koreatech.koin.domain.teamrecruitment.dto.DirectChatRoomResponse;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType.CHAT_ROOM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.NEW_CHAT_MESSAGE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentOutboxEventStatus.PENDING;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRecruitmentChatService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final String OUTBOX_EVENT_TYPE = "TEAM_RECRUITMENT_NOTIFICATION";
    private static final String AGGREGATE_TYPE = "TEAM_RECRUITMENT";
    private static final int DIRECT_MEMBER_COUNT = 2;
    private static final int TEAM_AUTHOR_COUNT = 1;

    private final TeamRecruitmentRepository recruitmentRepository;
    private final TeamRecruitmentApplicationRepository applicationRepository;
    private final TeamRecruitmentChatRoomRepository chatRoomRepository;
    private final TeamRecruitmentChatMemberRepository memberRepository;
    private final TeamRecruitmentChatMessageRepository messageRepository;
    private final TeamRecruitmentNotificationRepository notificationRepository;
    private final TeamRecruitmentOutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ChatRoomResponse getChatRoom(Integer userId, Integer recruitmentId, Integer chatRoomId) {
        TeamRecruitmentChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_CHAT_NOT_FOUND));

        if (!chatRoom.getRecruitment().getId().equals(recruitmentId)) {
            throw CustomException.of(TEAM_RECRUITMENT_CHAT_NOT_FOUND);
        }

        if (!memberRepository.existsByChatRoom_IdAndUser_Id(chatRoomId, userId)) {
            throw CustomException.of(TEAM_RECRUITMENT_CHAT_FORBIDDEN);
        }

        int memberCount = (int) memberRepository.countByChatRoom_Id(chatRoomId);

        ChatRoomResponse.Counterpart counterpart = null;
        if (chatRoom.getRoomType() == TeamRecruitmentChatRoomType.DIRECT) {
            counterpart = memberRepository.findAllByChatRoom_Id(chatRoomId).stream()
                    .filter(m -> !m.getUser().getId().equals(userId))
                    .findFirst()
                    .map(m -> new ChatRoomResponse.Counterpart(m.getUser().getId(), m.getUser().getNickname()))
                    .orElse(null);
        }

        String roomName = chatRoom.getRoomType() == TeamRecruitmentChatRoomType.DIRECT
                ? (counterpart != null ? counterpart.nickname() : "")
                : chatRoom.getRecruitment().getTitle();

        int maxMemberCount = chatRoom.getRoomType() == TeamRecruitmentChatRoomType.DIRECT
                ? DIRECT_MEMBER_COUNT
                : chatRoom.getRecruitment().getMaxParticipants() + TEAM_AUTHOR_COUNT;

        return ChatRoomResponse.of(chatRoom, roomName, memberCount, maxMemberCount, counterpart);
    }

    @Transactional
    public DirectChatRoomCreationResult getOrCreateDirectChatRoom(Integer userId, Integer recruitmentId, Integer applicationId) {
        TeamRecruitment recruitment = recruitmentRepository.findByIdWithLock(recruitmentId)
                .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_NOT_FOUND));

        TeamRecruitmentApplication application = applicationRepository
                .findByIdAndRecruitmentIdWithLock(applicationId, recruitmentId)
                .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_APPLICATION_NOT_FOUND));

        if (!application.getRecruitment().getId().equals(recruitmentId)) {
            throw CustomException.of(TEAM_RECRUITMENT_APPLICATION_NOT_FOUND);
        }

        if (!userId.equals(recruitment.getAuthor().getId())) {
            throw CustomException.of(TEAM_RECRUITMENT_FORBIDDEN);
        }

        if (application.getStatus() != ACCEPTED) {
            throw CustomException.of(TEAM_RECRUITMENT_APPLICATION_NOT_ACCEPTED);
        }

        User counterpartUser = application.getApplicant();
        Optional<TeamRecruitmentChatRoom> existingDirectChatRoom = chatRoomRepository
                .findByRecruitment_IdAndApplication_IdAndRoomType(
                        recruitmentId, applicationId, TeamRecruitmentChatRoomType.DIRECT);
        if (existingDirectChatRoom.isPresent()) {
            return new DirectChatRoomCreationResult(
                    DirectChatRoomResponse.of(existingDirectChatRoom.get(), counterpartUser), false);
        }

        TeamRecruitmentChatRoom teamChatRoom = chatRoomRepository
                .findByRecruitment_IdAndRoomScopeKey(recruitmentId, TeamRecruitmentChatRoom.TEAM_ROOM_SCOPE_KEY)
                .filter(room -> room.getRoomType() == TeamRecruitmentChatRoomType.TEAM)
                .orElse(null);
        LocalDate today = LocalDate.now(clock.withZone(KST));
        if (!TeamRecruitmentDirectChatPolicy.canOpenDirectChat(
                application.getStatus(), false, recruitment, teamChatRoom, today)) {
            throw CustomException.of(TEAM_RECRUITMENT_CLOSED);
        }

        TeamRecruitmentChatRoom chatRoom = TeamRecruitmentChatRoom.builder()
                .recruitment(recruitment)
                .roomScopeKey("DIRECT-" + applicationId)
                .roomType(TeamRecruitmentChatRoomType.DIRECT)
                .application(application)
                .build();
        chatRoom = chatRoomRepository.save(chatRoom);

        memberRepository.save(TeamRecruitmentChatMember.builder()
                .chatRoom(chatRoom).user(recruitment.getAuthor()).build());
        memberRepository.save(TeamRecruitmentChatMember.builder()
                .chatRoom(chatRoom).user(counterpartUser).build());

        return new DirectChatRoomCreationResult(DirectChatRoomResponse.of(chatRoom, counterpartUser), true);
    }

    @Transactional
    public List<ChatMessageResponse> getMessages(
            Integer userId, Integer recruitmentId, Integer chatRoomId,
            Integer afterMessageId, Integer beforeMessageId, int limit) {

        if (afterMessageId != null && beforeMessageId != null) {
            throw CustomException.of(ILLEGAL_ARGUMENT);
        }
        if (afterMessageId != null && afterMessageId < 1) {
            throw CustomException.of(ILLEGAL_ARGUMENT);
        }
        if (beforeMessageId != null && beforeMessageId < 1) {
            throw CustomException.of(ILLEGAL_ARGUMENT);
        }
        if (limit < 1 || limit > 200) {
            throw CustomException.of(ILLEGAL_ARGUMENT);
        }

        TeamRecruitmentChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_CHAT_NOT_FOUND));

        if (!chatRoom.getRecruitment().getId().equals(recruitmentId)) {
            throw CustomException.of(TEAM_RECRUITMENT_CHAT_NOT_FOUND);
        }

        TeamRecruitmentChatMember currentMember = memberRepository.findByChatRoom_IdAndUser_Id(chatRoomId, userId)
                .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_CHAT_FORBIDDEN));

        List<TeamRecruitmentChatMessage> messages = fetchMessages(chatRoomId, afterMessageId, beforeMessageId, limit);

        if (!messages.isEmpty()) {
            currentMember.advanceLastReadMessageId(messages.get(messages.size() - 1).getId());
        }

        List<TeamRecruitmentChatMember> allMembers = memberRepository.findAllByChatRoom_Id(chatRoomId);

        return messages.stream()
                .map(msg -> {
                    int unreadCount = (int) allMembers.stream()
                            .filter(m -> !m.getUser().getId().equals(msg.getSender().getId()))
                            .filter(m -> m.getLastReadMessageId() == null || m.getLastReadMessageId() < msg.getId())
                            .count();
                    return ChatMessageResponse.of(msg, unreadCount);
                })
                .toList();
    }

    private List<TeamRecruitmentChatMessage> fetchMessages(
            Integer chatRoomId, Integer afterMessageId, Integer beforeMessageId, int limit) {
        PageRequest pageable = PageRequest.of(0, limit);

        if (afterMessageId != null) {
            return messageRepository.findAllByChatRoom_IdAndIdGreaterThanOrderByIdAsc(chatRoomId, afterMessageId, pageable);
        }
        if (beforeMessageId != null) {
            List<TeamRecruitmentChatMessage> result = new ArrayList<>(
                    messageRepository.findAllByChatRoom_IdAndIdLessThanOrderByIdDesc(chatRoomId, beforeMessageId, pageable));
            Collections.reverse(result);
            return result;
        }
        return messageRepository.findAllByChatRoom_IdOrderByIdAsc(chatRoomId, pageable);
    }

    @Transactional
    public ChatMessageResponse createMessage(Integer userId, Integer recruitmentId, Integer chatRoomId, CreateChatMessageRequest request) {
        TeamRecruitmentChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_CHAT_NOT_FOUND));

        if (!chatRoom.getRecruitment().getId().equals(recruitmentId)) {
            throw CustomException.of(TEAM_RECRUITMENT_CHAT_NOT_FOUND);
        }

        TeamRecruitmentChatMember senderMember = memberRepository.findByChatRoom_IdAndUser_Id(chatRoomId, userId)
                .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_CHAT_FORBIDDEN));

        if (!chatRoom.isActive()) {
            throw CustomException.of(TEAM_RECRUITMENT_CHAT_READ_ONLY);
        }

        User sender = senderMember.getUser();

        TeamRecruitmentChatMessage message = messageRepository.save(
                TeamRecruitmentChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(sender)
                        .senderNickname(sender.getNickname())
                        .content(request.content())
                        .isImage(request.isImage())
                        .build());

        senderMember.advanceLastReadMessageId(message.getId());

        List<TeamRecruitmentChatMember> allMembers = memberRepository.findAllByChatRoom_Id(chatRoomId);
        int unreadCount = (int) allMembers.stream()
                .filter(m -> !m.getUser().getId().equals(userId))
                .filter(m -> m.getLastReadMessageId() == null || m.getLastReadMessageId() < message.getId())
                .count();

        saveChatMessageNotifications(message, chatRoom, allMembers, userId);

        return ChatMessageResponse.of(message, unreadCount);
    }

    private void saveChatMessageNotifications(
            TeamRecruitmentChatMessage message,
            TeamRecruitmentChatRoom chatRoom,
            List<TeamRecruitmentChatMember> allMembers,
            Integer senderId) {

        String messagePreview = message.getContent().length() <= 255
                ? message.getContent()
                : message.getContent().substring(0, 255);
        TeamRecruitment recruitment = chatRoom.getRecruitment();
        Integer applicationId = chatRoom.getApplication() == null ? null : chatRoom.getApplication().getId();

        for (TeamRecruitmentChatMember member : allMembers) {
            User recipient = member.getUser();
            if (recipient.getId().equals(senderId)) {
                continue;
            }
            String eventKey = "team-recruitment:chat-message:" + message.getId() + ":" + recipient.getId();
            if (outboxEventRepository.findByEventKey(eventKey).isPresent()) {
                continue;
            }

            TeamRecruitmentNotification notification = notificationRepository.save(
                    TeamRecruitmentNotification.builder()
                            .recipient(recipient)
                            .type(NEW_CHAT_MESSAGE)
                            .targetType(CHAT_ROOM)
                            .messagePreview(messagePreview)
                            .senderNickname(message.getSenderNickname())
                            .recruitment(recruitment)
                            .application(chatRoom.getApplication())
                            .chatRoom(chatRoom)
                            .build());

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("type", NEW_CHAT_MESSAGE.name());
            payload.put("target_type", CHAT_ROOM.name());
            payload.put("recipient_id", recipient.getId());
            payload.put("recruitment_id", recruitment.getId());
            payload.put("application_id", applicationId);
            payload.put("chat_room_id", chatRoom.getId());
            payload.put("notification_id", notification.getId());
            payload.put("message_preview", messagePreview);

            try {
                outboxEventRepository.save(TeamRecruitmentOutboxEvent.builder()
                        .eventKey(eventKey)
                        .eventType(OUTBOX_EVENT_TYPE)
                        .aggregateType(AGGREGATE_TYPE)
                        .aggregateId(recruitment.getId())
                        .payload(objectMapper.writeValueAsString(payload))
                        .status(PENDING)
                        .build());
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("채팅 메시지 알림 outbox payload를 직렬화할 수 없습니다.", e);
            }
        }
    }
}
