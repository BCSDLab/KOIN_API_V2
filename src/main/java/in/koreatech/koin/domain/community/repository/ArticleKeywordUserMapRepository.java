package in.koreatech.koin.domain.community.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.model.ArticleKeywordUserMap;

public interface ArticleKeywordUserMapRepository extends Repository<ArticleKeywordUserMap, Integer> {

    ArticleKeywordUserMap save(ArticleKeywordUserMap articleKeywordUserMap);

    Integer countByUserId(Integer userId);
}
