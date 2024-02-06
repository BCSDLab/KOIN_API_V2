package in.koreatech.koin.domain.community.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.model.HotArticle;

public interface HotArticleRepository extends Repository<HotArticle, Long> {

    void save(HotArticle hotArticle);

    HotArticle findById(Long id);

    List<HotArticle> findAll();

    void deleteById(Long id);
}
