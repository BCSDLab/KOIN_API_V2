package in.koreatech.koin.admin.abtest.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.abtest.dto.AbtestAssignRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestCloseRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestDevicesResponse;
import in.koreatech.koin.admin.abtest.dto.AbtestRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestResponse;
import in.koreatech.koin.admin.abtest.dto.AbtestUsersResponse;
import in.koreatech.koin.admin.abtest.dto.AbtestsResponse;
import in.koreatech.koin.admin.abtest.exception.AbtestAlreadyExistException;
import in.koreatech.koin.admin.abtest.exception.AbtestNotAssignedUserException;
import in.koreatech.koin.admin.abtest.model.Abtest;
import in.koreatech.koin.admin.abtest.model.AbtestStatus;
import in.koreatech.koin.admin.abtest.model.AbtestVariable;
import in.koreatech.koin.admin.abtest.model.AccessHistory;
import in.koreatech.koin.admin.abtest.model.AccessHistoryAbtestVariable;
import in.koreatech.koin.admin.abtest.model.Device;
import in.koreatech.koin.admin.abtest.model.redis.AbtestVariableCount;
import in.koreatech.koin.admin.abtest.model.redis.VariableIp;
import in.koreatech.koin.admin.abtest.repository.AbtestRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableCountRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableRepository;
import in.koreatech.koin.admin.abtest.repository.AccessHistoryAbtestVariableCustomRepository;
import in.koreatech.koin.admin.abtest.repository.AccessHistoryAbtestVariableRepository;
import in.koreatech.koin.admin.abtest.repository.AccessHistoryRepository;
import in.koreatech.koin.admin.abtest.repository.DeviceRepository;
import in.koreatech.koin.admin.abtest.repository.VariableIpRepository;
import in.koreatech.koin.admin.abtest.repository.VariableIpTemplateRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.model.Criteria;
import in.koreatech.koin.global.useragent.UserAgentInfo;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AbtestService {

    private final EntityManager entityManager;
    private final AbtestVariableCountRepository abtestVariableCountRepository;
    private final AbtestRepository abtestRepository;
    private final AbtestVariableRepository abtestVariableRepository;
    private final AccessHistoryRepository accessHistoryRepository;
    private final AccessHistoryAbtestVariableRepository accessHistoryAbtestVariableRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final VariableIpRepository variableIpRepository;
    private final VariableIpTemplateRepository variableIpTemplateRepository;
    private final AccessHistoryAbtestVariableCustomRepository accessHistoryAbtestVariableCustomRepository;

    @Transactional
    public AbtestResponse createAbtest(AbtestRequest request) {
        if (abtestRepository.findByTitle(request.title()).isPresent()) {
            throw AbtestAlreadyExistException.withDetail("title: " + request.title());
        }
        Abtest saved = abtestRepository.save(
            Abtest.builder()
                .title(request.title())
                .displayTitle(request.displayTitle())
                .description(request.description())
                .creator(request.creater())
                .team(request.team())
                .status(AbtestStatus.IN_PROGRESS)
                .build()
        );
        saved.setVariables(request.variables(), entityManager);
        return AbtestResponse.from(saved);
    }

    @Transactional
    public AbtestResponse putAbtest(Integer abtestId, AbtestRequest request) {
        Abtest foundAbtest = abtestRepository.getById(abtestId);
        foundAbtest.update(
            request.displayTitle(),
            request.creater(),
            request.team(),
            request.title(),
            request.description(),
            request.variables(),
            entityManager
        );
        syncCacheCountToDB(foundAbtest);
        deleteVariableIpCache(foundAbtest);
        modifyVariableByRate(foundAbtest);
        return AbtestResponse.from(foundAbtest);
    }

    private void modifyVariableByRate(Abtest abtest) {
        List<AbtestVariable> abtestVariables = abtest.getAbtestVariables();
        int totalRecords = abtestVariables.stream().mapToInt(AbtestVariable::getCount).sum();

        for (AbtestVariable variable : abtestVariables) {
            int targetCount = totalRecords * variable.getRate() / 100;
            int currentCount = variable.getCount();

            if (currentCount < targetCount) {
                int recordsToAdd = targetCount - currentCount;

                for (AbtestVariable otherVariable : abtestVariables) {
                    if (otherVariable.getId().equals(variable.getId())) {
                        continue;
                    }

                    int availableToMove = otherVariable.getCount() - (totalRecords * otherVariable.getRate() / 100);

                    if (availableToMove > 0) {
                        int moveCount = Math.min(recordsToAdd, availableToMove);

                        List<Integer> idsToMove =
                            accessHistoryAbtestVariableCustomRepository.findIdsToMove(otherVariable.getId(), moveCount);
                        accessHistoryAbtestVariableCustomRepository.updateVariableIds(idsToMove, variable.getId());

                        otherVariable.addCount(-idsToMove.size());
                        variable.addCount(idsToMove.size());
                        recordsToAdd -= idsToMove.size();

                        if (recordsToAdd <= 0) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private void deleteVariableIpCache(Abtest abtest) {
        abtest.getAbtestVariables().forEach(abtestVariable ->
            variableIpTemplateRepository.deleteByVariableId(abtestVariable.getId()));
    }

    @Transactional
    public void deleteAbtest(Integer abtestId) {
        abtestRepository.findById(abtestId).ifPresent(saved -> {
            syncCacheCountToDB(saved);
            deleteCacheCount(saved);
            deleteVariableIpCache(saved);
            abtestRepository.deleteById(abtestId);
        });
    }

    private void deleteCacheCount(Abtest abtest) {
        abtest.getAbtestVariables()
            .forEach(abtestVariable -> abtestVariableCountRepository.deleteById(abtestVariable.getId()));
    }

    public AbtestsResponse getAbtests(Integer page, Integer limit) {
        Long title = abtestRepository.countBy();
        Criteria criteria = Criteria.of(page, limit, title.intValue());
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.DESC, "id"));
        Page<Abtest> abtests = abtestRepository.findAll(pageRequest);
        return AbtestsResponse.of(abtests, criteria);
    }

    public AbtestResponse getAbtest(Integer abtestId) {
        return AbtestResponse.from(abtestRepository.getById(abtestId));
    }

    @Transactional
    public void closeAbtest(Integer abtestId, AbtestCloseRequest abtestCloseRequest) {
        Abtest abtest = abtestRepository.getById(abtestId);
        abtest.close(abtestCloseRequest.winnerName());
        syncCacheCountToDB(abtest);
        deleteCacheCount(abtest);
        deleteVariableIpCache(abtest);
    }

    @Transactional
    public String assignVariable(UserAgentInfo userAgentInfo, String ipAddress, Integer userId,
        AbtestAssignRequest request) {
        Abtest abtest = abtestRepository.getByTitle(request.title());
        List<AbtestVariableCount> cacheCount = abtest.getAbtestVariables().stream()
            .map(abtestVariable -> abtestVariableCountRepository.getById(abtestVariable.getId()))
            .toList();
        // 편입 대상이 되는 변수 선정
        AbtestVariable variable = abtest.assignVariable(cacheCount);

        // 로그인 유저에게 디바이스가 없으면 생성
        if (userId != null && deviceRepository.findByUserId(userId).isEmpty()) {
            deviceRepository.save(
                Device.builder()
                    .user(userRepository.getById(userId))
                    .model(userAgentInfo.getModel())
                    .type(userAgentInfo.getType())
                    .build()
            );
        }

        // 연결 기록 없으면 생성
        if (accessHistoryRepository.findByPublicIp(ipAddress).isEmpty()) {
            AccessHistory accessHistory = AccessHistory.builder()
                .publicIp(ipAddress)
                .build();
            accessHistoryRepository.save(accessHistory);
        }

        // 로그인 유저가 연결기록 - 디바이스 매핑이 안되어있으면 매핑 진행
        AccessHistory accessHistory = accessHistoryRepository.getByPublicIp(ipAddress);
        if (userId != null) {
            Device device = deviceRepository.getByUserId(userId);
            if (accessHistoryRepository.findByDevice(device).isEmpty()) {
                accessHistory.connectDevice(device);
            }
        }

        // 연결 이력 - 변수 연결
        // TODO: 연관관계 편입 메서드로 분리하기
        if (!accessHistory.hasVariable(variable.getId())) {
            AccessHistoryAbtestVariable saved = accessHistoryAbtestVariableRepository.save(
                AccessHistoryAbtestVariable.builder()
                    .accessHistory(accessHistory)
                    .variable(variable)
                    .build()
            );
            accessHistory.getAccessHistoryAbtestVariables().add(saved);
            variable.getAccessHistoryAbtestVariables().add(saved);
        }
        // TODO: 캐싱해두기
        return variable.getName();
    }

    @Transactional
    public String getMyVariable(String title, String ipAddress) {
        Abtest abtest = abtestRepository.getByTitle(title);
        Optional<AbtestVariable> cacheVariable = abtest.getAbtestVariables().stream()
            .filter(abtestVariable ->
                variableIpRepository.findByVariableIdAndIp(abtestVariable.getId(), ipAddress).isPresent())
            .findAny();

        if (cacheVariable.isEmpty()) {
            AbtestVariable dbVariable = accessHistoryRepository.findByPublicIp(ipAddress)
                .orElseThrow(() -> AbtestNotAssignedUserException.withDetail("abtestId: " + abtest.getId() + ", "
                    + "publicIp: " + ipAddress))
                .findVariableByAbtestId(abtest.getId())
                .orElseThrow(() -> AbtestNotAssignedUserException.withDetail("abtestId: " + abtest.getId() + ", "
                    + "publicIp: " + ipAddress));
            variableIpRepository.save(VariableIp.of(dbVariable.getId(), ipAddress));
            return dbVariable.getName();
        }

        return cacheVariable.get().getName();
    }

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
    public void syncCacheCountToDB(Abtest abtest) {
        List<AbtestVariableCount> cacheCount = abtest.getAbtestVariables().stream()
            .map(AbtestVariable::getId)
            .map(abtestVariableCountRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
        cacheCount.forEach(abtestVariableCount -> {
            AbtestVariable variable = abtestVariableRepository.getById(abtestVariableCount.getVariableId());
            variable.addCount(abtestVariableCount.getCount());
            abtestVariableCount.resetCount();
        });
        abtestVariableCountRepository.saveAll(cacheCount);
    }

    public AbtestUsersResponse getUsersByName(String userName) {
        return AbtestUsersResponse.from(userRepository.findAllByName(userName));
    }

    public AbtestDevicesResponse getDevicesByUserId(Integer userId) {
        User saved = userRepository.getById(userId);
        return AbtestDevicesResponse.from(deviceRepository.findAllByUserId(saved.getId()));
    }
}
