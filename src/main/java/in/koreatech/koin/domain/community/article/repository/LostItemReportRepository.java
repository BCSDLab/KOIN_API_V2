package in.koreatech.koin.domain.community.article.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin.domain.community.article.model.LostItemReport;

public interface LostItemReportRepository extends Repository<LostItemReport, Integer> {

    LostItemReport save(LostItemReport lostItemReport);

    Optional<LostItemReport> findById(Integer id);

    List<LostItemReport> findAllByLostItemArticle(LostItemArticle lostItemArticle);
}
