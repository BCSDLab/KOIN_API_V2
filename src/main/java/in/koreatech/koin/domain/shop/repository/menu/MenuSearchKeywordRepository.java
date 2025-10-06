package in.koreatech.koin.domain.shop.repository.menu;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.shop.model.menu.MenuSearchKeyWord;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface MenuSearchKeywordRepository extends Repository<MenuSearchKeyWord, Integer> {

    @Query("SELECT DISTINCT m.keyword FROM MenuSearchKeyWord m WHERE m.keyword LIKE %:query%")
    List<String> findDistinctNameContains(@Param("query") String query);

    MenuSearchKeyWord save(MenuSearchKeyWord menuSearchKeyWord);
}
