package in.koreatech.koin.admin.abtest.service;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.admin.abtest.model.AbtestCount;
import in.koreatech.koin.admin.abtest.repository.AbtestCountRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestIpRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AbtestService {

    private final AbtestCountRepository abtestCountRepository;
    private final AbtestIpRepository abtestIpRepository;
    private final AbtestRepository abtestRepository;

    public void syncCacheCountToDB() {
        List<AbtestCount> abtestCounts = abtestCountRepository.findAll();

        abtestCounts.stream()
            .forEach(abtestCount -> {
                abtestRepository.getById(abtestCount.getId());
            });
    }
}
