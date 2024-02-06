package in.koreatech.koin.domain.community.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.model.HotArticle;

public interface HotArticleRepository extends Repository<HotArticle, Long> {

    void save(HotArticle hotArticle);

    void deleteById(Long id);
    HotArticle findById(Long id);
}
