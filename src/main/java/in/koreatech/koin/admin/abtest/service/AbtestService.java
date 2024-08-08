package in.koreatech.koin.admin.abtest.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.abtest.model.Abtest;
import in.koreatech.koin.admin.abtest.model.AbtestIp;
import in.koreatech.koin.admin.abtest.model.AbtestVariableCount;
import in.koreatech.koin.admin.abtest.model.AbtestVariable;
import in.koreatech.koin.admin.abtest.model.AccessHistory;
import in.koreatech.koin.admin.abtest.model.AccessHistoryAbtestVariable;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableCountRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestIpRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableRepository;
import in.koreatech.koin.admin.abtest.repository.AccessHistoryAbtestVariableRepository;
import in.koreatech.koin.admin.abtest.repository.AccessHistoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AbtestService {

    private final AbtestVariableCountRepository abtestVariableCountRepository;
    private final AbtestIpRepository abtestIpRepository;
    private final AbtestRepository abtestRepository;
    private final AbtestVariableRepository abtestVariableRepository;
    private final AccessHistoryRepository accessHistoryRepository;
    private final AccessHistoryAbtestVariableRepository accessHistoryAbtestVariableRepository;

    @Transactional
    public void syncCacheCountToDB() {
        List<AbtestVariableCount> cacheCount = abtestVariableCountRepository.findAll();
        cacheCount.forEach(abtestVariableCount -> {
            AbtestVariable variable = abtestVariableRepository.getById(abtestVariableCount.getVariableId());
            variable.addCount(abtestVariableCount.getCount());
            abtestVariableCount.resetCount();
        });
        abtestVariableCountRepository.saveAll(cacheCount);
    }

    @Transactional
    public String assignVariable(String title, String ipAddress) {
        Abtest abtest = abtestRepository.getByTitle(title);
        List<AbtestVariableCount> cacheCount = abtest.getAbtestVariables().stream()
            .map(abtestVariable -> abtestVariableCountRepository.getById(abtestVariable.getId()))
            .toList();
        AbtestVariable variable = abtest.assignVariable(cacheCount);

        // 실제 편입 진행
        /**
         * findByIp(..)
         * 있으면 그걸로 variable, variable map table
         * 없으면 새로 만든 후에 위 과정 진행.
         */

        Optional<AccessHistory> accessHistory = accessHistoryRepository.findByPublicIp(ipAddress);

        if (accessHistory.isEmpty()) {
            // 액세스 히스토리 생성 (디바이스는?)
        }

        // TODO: 연관관계 편입 메서드로 분리하기
        // variable.addAccessHistory(accessHistory.get());
        AccessHistoryAbtestVariable saved = accessHistoryAbtestVariableRepository.save(
            AccessHistoryAbtestVariable.builder()
                .accessHistory(accessHistory.get())
                .variable(variable)
                .build()
        );
        accessHistory.get().getAccessHistoryAbtestVariables().add(saved);
        variable.getAccessHistoryAbtestVariables().add(saved);

        return variable.getName();
    }

    public String getMyVariable(String title, String ipAddress) {
        Abtest abtest = abtestRepository.getByTitle(title);
        AbtestIp abtestIp = abtestIpRepository.getById(abtest.getId());
        int variableId = abtestIp.getVariableIdByIp(ipAddress);
        AbtestVariable variable = abtestVariableRepository.getById(variableId);
        // TODO: 캐시가 없으면 DB에서 꺼내서 캐시에 집어넣기
        return variable.getName();
    }
}
