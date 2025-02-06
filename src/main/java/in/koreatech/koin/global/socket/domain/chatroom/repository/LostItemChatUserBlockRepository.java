package in.koreatech.koin.global.socket.domain.chatroom.repository;

import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import in.koreatech.koin.global.socket.domain.chatroom.model.LostItemChatUserBlockEntity;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LostItemChatUserBlockRepository {

    private final MongoTemplate mongoTemplate;

    public LostItemChatUserBlockEntity save(LostItemChatUserBlockEntity entity) {
        return mongoTemplate.save(entity);
    }

    public Optional<LostItemChatUserBlockEntity> findByBlockerUserAndBlockedUser(
        Integer blockerUserId,
        Integer blockedUserId
    ) {
        Query query = Query.query(Criteria.where("blocker_user_id").is(blockerUserId)
            .and("blocked_user_id").is(blockedUserId)
            .and("is_active").is(true)
        );

        LostItemChatUserBlockEntity result = mongoTemplate.findOne(query, LostItemChatUserBlockEntity.class);
        return Optional.ofNullable(result);
    }
}
