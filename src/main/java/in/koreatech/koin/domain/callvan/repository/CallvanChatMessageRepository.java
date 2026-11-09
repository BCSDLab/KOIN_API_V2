package in.koreatech.koin.domain.callvan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.callvan.model.CallvanChatMessage;

public interface CallvanChatMessageRepository extends Repository<CallvanChatMessage, Integer> {

    CallvanChatMessage save(CallvanChatMessage callvanChatMessage);

    List<CallvanChatMessage> findAllByChatRoomIdOrderByCreatedAtAsc(Integer chatRoomId);

    @Query("""
        SELECT m FROM CallvanChatMessage m
        JOIN m.chatRoom r JOIN r.post p
        WHERE m.id = :messageId AND p.id = :postId
          AND r.isDeleted = false AND p.isDeleted = false
        """)
    Optional<CallvanChatMessage> findInActivePost(@Param("messageId") Integer messageId, @Param("postId") Integer postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CallvanChatMessage c SET c.isLeftUser = :isLeftUser WHERE c.chatRoom.id = :chatRoomId AND c.sender.id = :senderId")
    void updateIsLeftUserByChatRoomIdAndSenderId(
            @Param("chatRoomId") Integer chatRoomId,
            @Param("senderId") Integer senderId,
            @Param("isLeftUser") boolean isLeftUser
    );
}
