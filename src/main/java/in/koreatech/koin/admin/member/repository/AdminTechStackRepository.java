package in.koreatech.koin.admin.member.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.member.model.TechStack;

public interface AdminTechStackRepository extends Repository<TechStack, Integer> {

    TechStack save(TechStack techStack);
}
