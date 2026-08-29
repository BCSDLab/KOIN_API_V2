package in.koreatech.koin.domain.teamrecruitment.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import in.koreatech.koin.domain.teamrecruitment.dto.ChatMessageResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.ChatRoomResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.CreateChatMessageRequest;
import in.koreatech.koin.domain.teamrecruitment.dto.DirectChatRoomResponse;
import in.koreatech.koin.domain.teamrecruitment.model.TeamRecruitmentChatMessage;
import in.koreatech.koin.domain.teamrecruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.teamrecruitment.model.TeamRecruitmentChatRoomMember;
import in.koreatech.koin.domain.teamrecruitment.model.enums.ChatRoomStatus;
import in.koreatech.koin.domain.teamrecruitment.model.enums.ChatRoomType;
import in.koreatech.koin.domain.teamrecruitment.repository.TeamRecruitmentChatMessageRepository;
import in.koreatech.koin.domain.teamrecruitment.repository.TeamRecruitmentChatRoomMemberRepository;
import in.koreatech.koin.domain.teamrecruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRecruitmentChatService {

    private final TeamRecruitmentChatRoomRepository chatRoomRepository;
    private final TeamRecruitmentChatRoomMemberRepository memberRepository;
    private final TeamRecruitmentChatMessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatRoomResponse getChatRoom(Integer userId, Integer recruitmentId, Integer chatRoomId) {
        TeamRecruitmentChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        if (!memberRepository.existsByChatRoomIdAndUserId(chatRoomId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "채팅방 멤버가 아닙니다.");
        }

        int memberCount = memberRepository.countByChatRoomId(chatRoomId);

        // DIRECT 채팅방이면 상대방 정보 조회
        ChatRoomResponse.Counterpart counterpart = null;
        if (chatRoom.getRoomType() == ChatRoomType.DIRECT) {
            counterpart = memberRepository.findAllByChatRoomId(chatRoomId).stream()
                    .filter(m -> !m.getUser().getId().equals(userId))
                    .findFirst()
                    .map(m -> new ChatRoomResponse.Counterpart(m.getUser().getId(), m.getUser().getNickname()))
                    .orElse(null);
        }

        return ChatRoomResponse.of(chatRoom, memberCount, counterpart);
    }

    @Transactional
    public DirectChatRoomResponse getOrCreateDirectChatRoom(Integer userId, Integer recruitmentId, Integer applicantUserId) {
        // 이미 존재하는 DIRECT 채팅방 확인
        return chatRoomRepository.findDirectChatRoom(recruitmentId, userId, applicantUserId, ChatRoomType.DIRECT)
                .map(existing -> {
                    User counterpart = userRepository.findById(applicantUserId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));
                    return DirectChatRoomResponse.of(existing, counterpart);
                })
                .orElseGet(() -> {
                    // 신규 DIRECT 채팅방 생성
                    User author = userRepository.findById(userId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));
                    User applicant = userRepository.findById(applicantUserId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

                    TeamRecruitmentChatRoom chatRoom = TeamRecruitmentChatRoom.builder()
                            .recruitmentId(recruitmentId)
                            .roomName(applicant.getNickname())
                            .roomType(ChatRoomType.DIRECT)
                            .maxMemberCount(2)
                            .build();
                    chatRoomRepository.save(chatRoom);

                    memberRepository.save(TeamRecruitmentChatRoomMember.builder()
                            .chatRoom(chatRoom).user(author).lastReadMessageId(null).build());
                    memberRepository.save(TeamRecruitmentChatRoomMember.builder()
                            .chatRoom(chatRoom).user(applicant).lastReadMessageId(null).build());

                    return DirectChatRoomResponse.of(chatRoom, applicant);
                });
    }

    @Transactional
    public List<ChatMessageResponse> getMessages(
            Integer userId, Integer chatRoomId,
            Integer afterMessageId, Integer beforeMessageId, int limit) {

        TeamRecruitmentChatRoomMember currentMember = memberRepository.findByChatRoomIdAndUserId(chatRoomId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "채팅방 멤버가 아닙니다."));

        List<TeamRecruitmentChatMessage> messages = fetchMessages(chatRoomId, afterMessageId, beforeMessageId, limit);

        // 메시지 조회 시 마지막 읽은 메시지 ID 갱신
        if (!messages.isEmpty()) {
            Integer lastMessageId = messages.get(messages.size() - 1).getId();
            currentMember.updateLastReadMessageId(lastMessageId);
        }

        // unread_count 계산을 위해 전체 멤버 읽음 위치 조회
        List<TeamRecruitmentChatRoomMember> allMembers = memberRepository.findAllByChatRoomId(chatRoomId);

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
            return messageRepository.findByChatRoomIdAndIdGreaterThanOrderByIdAsc(chatRoomId, afterMessageId, pageable);
        }
        if (beforeMessageId != null) {
            // 과거 메시지는 DESC로 가져온 뒤 오름차순으로 뒤집기
            List<TeamRecruitmentChatMessage> result = new ArrayList<>(
                    messageRepository.findByChatRoomIdAndIdLessThanOrderByIdDesc(chatRoomId, beforeMessageId, pageable));
            Collections.reverse(result);
            return result;
        }
        // 초기 조회: 최신 메시지를 DESC로 가져온 뒤 오름차순으로 뒤집기
        List<TeamRecruitmentChatMessage> result = new ArrayList<>(
                messageRepository.findByChatRoomIdOrderByIdDesc(chatRoomId, pageable));
        Collections.reverse(result);
        return result;
    }

    @Transactional
    public ChatMessageResponse createMessage(Integer userId, Integer chatRoomId, CreateChatMessageRequest request) {
        TeamRecruitmentChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        if (!memberRepository.existsByChatRoomIdAndUserId(chatRoomId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "채팅방 멤버가 아닙니다.");
        }

        if (chatRoom.getStatus() == ChatRoomStatus.READ_ONLY) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "읽기 전용 채팅방입니다.");
        }

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        TeamRecruitmentChatMessage message = TeamRecruitmentChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(request.content())
                .isImage(request.isImage())
                .build();
        messageRepository.save(message);

        // 발신자의 lastReadMessageId 갱신
        memberRepository.findByChatRoomIdAndUserId(chatRoomId, userId)
                .ifPresent(m -> m.updateLastReadMessageId(message.getId()));

        // 발신자 제외 다른 멤버 중 안 읽은 수
        List<TeamRecruitmentChatRoomMember> allMembers = memberRepository.findAllByChatRoomId(chatRoomId);
        int unreadCount = (int) allMembers.stream()
                .filter(m -> !m.getUser().getId().equals(userId))
                .filter(m -> m.getLastReadMessageId() == null || m.getLastReadMessageId() < message.getId())
                .count();

        return ChatMessageResponse.of(message, unreadCount);
    }
}
