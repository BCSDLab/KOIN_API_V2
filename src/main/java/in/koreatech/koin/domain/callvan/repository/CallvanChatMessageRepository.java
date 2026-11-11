package in.koreatech.koin.domain.callvan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.callvan.model.CallvanChatMessage;

public interface CallvanChatMessageRepository extends Repository<CallvanChatMessage, Integer> {

    Optional<CallvanChatMessage> findFirstByChatRoomIdOrderByIdDesc(Integer chatRoomId);

    List<CallvanChatMessage> findByChatRoomIdOrderByIdAsc(Integer chatRoomId, Pageable pageable);

    List<CallvanChatMessage> findByChatRoomIdAndIdGreaterThanOrderByIdAsc(
        Integer chatRoomId, Integer afterMessageId, Pageable pageable);

    List<CallvanChatMessage> findByChatRoomIdAndIdLessThanOrderByIdDesc(
        Integer chatRoomId, Integer beforeMessageId, Pageable pageable);

    List<CallvanChatMessage> findByChatRoomIdOrderByIdDesc(Integer chatRoomId, Pageable pageable);

    boolean existsByIdAndChatRoomId(Integer messageId, Integer chatRoomId);

    @Query("""
        SELECT COUNT(message) FROM CallvanChatMessage message
        WHERE message.chatRoom.id = :roomId AND message.sender.id <> :memberId
            AND message.id > (SELECT participant.lastReadMessageId FROM CallvanParticipant participant
                WHERE participant.post.chatRoom.id = :roomId AND participant.member.id = :memberId
                    AND participant.isDeleted = false)
        """)
    int countUnreadMessages(@Param("roomId") Integer roomId, @Param("memberId") Integer memberId);

    CallvanChatMessage save(CallvanChatMessage callvanChatMessage);

    List<CallvanChatMessage> findAllByChatRoomIdOrderByCreatedAtAsc(Integer chatRoomId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CallvanChatMessage c SET c.isLeftUser = :isLeftUser WHERE c.chatRoom.id = :chatRoomId AND c.sender.id = :senderId")
    void updateIsLeftUserByChatRoomIdAndSenderId(
            @Param("chatRoomId") Integer chatRoomId,
            @Param("senderId") Integer senderId,
            @Param("isLeftUser") boolean isLeftUser
    );
}
