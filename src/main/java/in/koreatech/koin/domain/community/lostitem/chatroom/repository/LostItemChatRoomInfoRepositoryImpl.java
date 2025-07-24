package in.koreatech.koin.domain.community.lostitem.chatroom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatRoomInfoEntity;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LostItemChatRoomInfoRepositoryImpl implements LostItemChatRoomInfoRepository{

    private final MongoTemplate mongoTemplate;

    public LostItemChatRoomInfoEntity save(LostItemChatRoomInfoEntity chatRoomInfo) {
        return mongoTemplate.save(chatRoomInfo);
    }

    public List<LostItemChatRoomInfoEntity> findByUserId(Integer userId) {
        Criteria authorCriteria = Criteria.where("article_author_id").is(userId);
        Criteria ownerCriteria = Criteria.where("item_owner_id").is(userId);

        Criteria combinedCriteria = new Criteria().orOperator(authorCriteria, ownerCriteria);
        Query query = Query.query(combinedCriteria);

        return mongoTemplate.find(query, LostItemChatRoomInfoEntity.class);
    }

    public Optional<LostItemChatRoomInfoEntity> findByArticleIdAndMessageSenderId(
        Integer articleId, Integer messageSenderId
    ) {
        Query query = Query.query(Criteria.where("article_id").is(articleId)
            .and("item_owner_id").is(messageSenderId));

        LostItemChatRoomInfoEntity result = mongoTemplate.findOne(query, LostItemChatRoomInfoEntity.class);
        return Optional.ofNullable(result);
    }

    public List<LostItemChatRoomInfoEntity> findByArticleId(Integer articleId) {
        Query query = Query.query(Criteria.where("article_id").is(articleId));

        return mongoTemplate.find(query, LostItemChatRoomInfoEntity.class);
    }

    public Optional<LostItemChatRoomInfoEntity> findByArticleIdAndChatRoomId(Integer articleId, Integer chatRoomId) {
        Query query = Query.query(Criteria.where("article_id").is(articleId)
            .and("chatroom_id").is(chatRoomId));

        LostItemChatRoomInfoEntity result = mongoTemplate.findOne(query, LostItemChatRoomInfoEntity.class);
        return Optional.ofNullable(result);
    }

    public LostItemChatRoomInfoEntity getByArticleIdAndChatRoomId(Integer articleId, Integer chatRoomId) {
        return findByArticleIdAndChatRoomId(articleId, chatRoomId).orElseThrow(
            () -> CustomException.of(ApiResponseCode.NOT_FOUND_LOST_ITEM_CHATROOM)
        );
    }
}
