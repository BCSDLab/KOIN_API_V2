package in.koreatech.koin.batch.campus.koreatech.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.batch.campus.koreatech.model.HttpCookieJar;

public interface HttpCookieJarRepository extends Repository<HttpCookieJar, String> {

    HttpCookieJar save(HttpCookieJar cookies);

    Optional<HttpCookieJar> findById(String id);

    default HttpCookieJar getById(String id) {
        return findById(id)
            .orElseThrow(() -> new RuntimeException("Cookie not found"));
    }

}
