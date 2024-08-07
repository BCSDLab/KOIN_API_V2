package in.koreatech.koin.admin.abtest.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.abtest.model.Abtest;
import in.koreatech.koin.admin.abtest.model.AbtestCount;
import in.koreatech.koin.admin.abtest.model.AbtestVariable;
import in.koreatech.koin.admin.abtest.repository.AbtestCountRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestIpRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AbtestService {

    private final AbtestCountRepository abtestCountRepository;
    private final AbtestIpRepository abtestIpRepository;
    private final AbtestRepository abtestRepository;
    private final AbtestVariableRepository abtestVariableRepository;

    @Transactional
    public void syncCacheCountToDB() {
        List<AbtestCount> abtestCounts = abtestCountRepository.findAll();
        abtestCounts.forEach(abtestCount -> {
            AbtestVariable variable = abtestVariableRepository.getById(abtestCount.getVariableId());
            variable.addCount(abtestCount.getCount());
            abtestCount.resetCount();
        });
        abtestCountRepository.saveAll(abtestCounts);
    }
}
