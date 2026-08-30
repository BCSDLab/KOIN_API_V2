package in.koreatech.koin.domain.teamrecruitment.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.teamrecruitment.dto.ChatMessageResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.ChatRoomResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.CreateChatMessageRequest;
import in.koreatech.koin.domain.teamrecruitment.dto.DirectChatRoomResponse;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static in.koreatech.koin.global.code.ApiResponseCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRecruitmentChatService {

    private final TeamRecruitmentRepository recruitmentRepository;
    private final TeamRecruitmentApplicationRepository applicationRepository;
    private final TeamRecruitmentChatRoomRepository chatRoomRepository;
    private final TeamRecruitmentChatMemberRepository memberRepository;
    private final TeamRecruitmentChatMessageRepository messageRepository;

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

        int maxMemberCount = chatRoom.getRecruitment().getMaxParticipants();

        return ChatRoomResponse.of(chatRoom, roomName, memberCount, maxMemberCount, counterpart);
    }

    @Transactional
    public DirectChatRoomResponse getOrCreateDirectChatRoom(Integer userId, Integer recruitmentId, Integer applicationId) {
        TeamRecruitmentApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_APPLICATION_NOT_FOUND));

        TeamRecruitment recruitment = recruitmentRepository.findById(recruitmentId)
                .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_NOT_FOUND));

        if (!application.getRecruitment().getId().equals(recruitmentId)) {
            throw CustomException.of(TEAM_RECRUITMENT_APPLICATION_NOT_FOUND);
        }

        if (!userId.equals(recruitment.getAuthor().getId())) {
            throw CustomException.of(TEAM_RECRUITMENT_FORBIDDEN);
        }

        User counterpartUser = application.getApplicant();

        return chatRoomRepository.findByRecruitment_IdAndApplication_IdAndRoomType(
                        recruitmentId, applicationId, TeamRecruitmentChatRoomType.DIRECT)
                .map(existing -> DirectChatRoomResponse.of(existing, counterpartUser))
                .orElseGet(() -> {
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

                    return DirectChatRoomResponse.of(chatRoom, counterpartUser);
                });
    }

    @Transactional
    public List<ChatMessageResponse> getMessages(
            Integer userId, Integer chatRoomId,
            Integer afterMessageId, Integer beforeMessageId, int limit) {

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
    public ChatMessageResponse createMessage(Integer userId, Integer chatRoomId, CreateChatMessageRequest request) {
        TeamRecruitmentChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_CHAT_NOT_FOUND));

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

        return ChatMessageResponse.of(message, unreadCount);
    }
}
