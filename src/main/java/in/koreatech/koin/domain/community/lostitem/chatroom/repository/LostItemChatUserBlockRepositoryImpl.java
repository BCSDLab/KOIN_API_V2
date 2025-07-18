package in.koreatech.koin.domain.community.lostitem.chatroom.repository;

import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatUserBlockEntity;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LostItemChatUserBlockRepositoryImpl implements LostItemChatUserBlockRepository {

    private final MongoTemplate mongoTemplate;

    public LostItemChatUserBlockEntity save(LostItemChatUserBlockEntity entity) {
        return mongoTemplate.save(entity);
    }

    public Optional<LostItemChatUserBlockEntity> findByBlockerUserAndBlockedUserAndIsActive(
        Integer blockerUserId,
        Integer blockedUserId,
        Boolean isActive
    ) {
        Query query = Query.query(Criteria.where("blocker_user_id").is(blockerUserId)
            .and("blocked_user_id").is(blockedUserId)
            .and("is_active").is(isActive)
        );

        LostItemChatUserBlockEntity result = mongoTemplate.findOne(query, LostItemChatUserBlockEntity.class);
        return Optional.ofNullable(result);
    }

    public void updateIsActiveByBlockerAndBlockedUser(
        Integer blockerUserId,
        Integer blockedUserId,
        Boolean isActive
    ) {
        Query query = Query.query(Criteria.where("blocker_user_id").is(blockerUserId)
            .and("blocked_user_id").is(blockedUserId));

        Update update = new Update().set("is_active", isActive);

        mongoTemplate.updateFirst(query, update, LostItemChatUserBlockEntity.class);
    }
}
