package in.koreatech.koin.domain.club.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.model.Club;

public interface ClubRepository extends Repository<Club, Integer> {

    List<Club> findAll();
}
