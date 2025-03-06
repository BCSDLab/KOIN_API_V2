package in.koreatech.koin.socket.domain.chatroom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import in.koreatech.koin.socket.domain.chatroom.model.LostItemChatMessageEntity;
import in.koreatech.koin.socket.domain.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.socket.domain.message.model.ChatMessageEntity;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LostItemChatRoomInfoRepository {

    private final MongoTemplate mongoTemplate;

    public LostItemChatRoomInfoEntity save(LostItemChatRoomInfoEntity chatRoomInfo) {
        return mongoTemplate.save(chatRoomInfo);
    }

    public List<LostItemChatRoomInfoEntity> findByArticleId(Integer articleId) {
        Query query = Query.query(Criteria.where("article_id").is(articleId));

        return mongoTemplate.find(query, LostItemChatRoomInfoEntity.class);
    }

    public Optional<LostItemChatRoomInfoEntity> findByArticleIdAndOwnerId(Integer articleId, Integer ownerId) {
        Query query = Query.query(Criteria.where("article_id").is(articleId)
            .and("item_owner_id").is(ownerId));

        LostItemChatRoomInfoEntity result = mongoTemplate.findOne(query, LostItemChatRoomInfoEntity.class);
        return Optional.ofNullable(result);
    }

    public List<LostItemChatRoomInfoEntity> findByUserId(Integer userId) {
        Criteria authorCriteria = Criteria.where("article_author_id").is(userId);
        Criteria ownerCriteria = Criteria.where("item_owner_id").is(userId);

        Criteria combinedCriteria = new Criteria().orOperator(authorCriteria, ownerCriteria);
        Query query = Query.query(combinedCriteria);

        return mongoTemplate.find(query, LostItemChatRoomInfoEntity.class);
    }

    public Optional<LostItemChatRoomInfoEntity> findByArticleIdAndChatRoomId(Integer articleId, Integer chatRoomId) {
        Query query = Query.query(Criteria.where("article_id").is(articleId)
            .and("chatroom_id").is(chatRoomId));

        LostItemChatRoomInfoEntity result = mongoTemplate.findOne(query, LostItemChatRoomInfoEntity.class);
        return Optional.ofNullable(result);
    }

    public LostItemChatRoomInfoEntity getByArticleIdAndChatRoomId(Integer articleId, Integer chatRoomId) {
        return findByArticleIdAndChatRoomId(articleId, chatRoomId).orElseThrow();
    }

    public void insertMessage(Integer articleId, Integer chatRoomId, ChatMessageEntity message) {
        Query query = Query.query(Criteria.where("article_id").is(articleId)
            .and("chatroom_id").is(chatRoomId));

        Update update = new Update();

        LostItemChatMessageEntity newMessage = LostItemChatMessageEntity.from(message);

        update.push("messages", newMessage);

        mongoTemplate.updateFirst(query, update, LostItemChatRoomInfoEntity.class);
    }
}
