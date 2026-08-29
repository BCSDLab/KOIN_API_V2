package in.koreatech.koin.domain.teamrecruitment.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMessage;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
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
import lombok.RequiredArgsConstructor;

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        if (!chatRoom.getRecruitment().getId().equals(recruitmentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다.");
        }

        if (!memberRepository.existsByChatRoom_IdAndUser_Id(chatRoomId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "채팅방 멤버가 아닙니다.");
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "지원서를 찾을 수 없습니다."));

        TeamRecruitment recruitment = recruitmentRepository.findById(recruitmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "모집글을 찾을 수 없습니다."));

        if (!userId.equals(recruitment.getAuthor().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "채팅방 멤버가 아닙니다."));

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        TeamRecruitmentChatMember senderMember = memberRepository.findByChatRoom_IdAndUser_Id(chatRoomId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "채팅방 멤버가 아닙니다."));

        if (!chatRoom.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "읽기 전용 채팅방입니다.");
        }

        User sender = senderMember.getUser();

        TeamRecruitmentChatMessage message = TeamRecruitmentChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .senderNickname(sender.getNickname())
                .content(request.content())
                .isImage(request.isImage())
                .build();
        messageRepository.save(message);

        senderMember.advanceLastReadMessageId(message.getId());

        List<TeamRecruitmentChatMember> allMembers = memberRepository.findAllByChatRoom_Id(chatRoomId);
        int unreadCount = (int) allMembers.stream()
                .filter(m -> !m.getUser().getId().equals(userId))
                .filter(m -> m.getLastReadMessageId() == null || m.getLastReadMessageId() < message.getId())
                .count();

        return ChatMessageResponse.of(message, unreadCount);
    }
}
