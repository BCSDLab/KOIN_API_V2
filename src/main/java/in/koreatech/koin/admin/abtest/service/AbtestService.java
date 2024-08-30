package in.koreatech.koin.admin.abtest.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.abtest.dto.AbtestAdminAssignRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestAssignRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestCloseRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestDevicesResponse;
import in.koreatech.koin.admin.abtest.dto.AbtestRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestResponse;
import in.koreatech.koin.admin.abtest.dto.AbtestUsersResponse;
import in.koreatech.koin.admin.abtest.dto.AbtestsResponse;
import in.koreatech.koin.admin.abtest.exception.AbtestAlreadyExistException;
import in.koreatech.koin.admin.abtest.exception.AbtestAssignedUserException;
import in.koreatech.koin.admin.abtest.exception.AbtestDuplicatedVariableException;
import in.koreatech.koin.admin.abtest.exception.AbtestNotAssignedUserException;
import in.koreatech.koin.admin.abtest.exception.AbtestNotInProgressException;
import in.koreatech.koin.admin.abtest.exception.AbtestWinnerNotDecidedException;
import in.koreatech.koin.admin.abtest.model.Abtest;
import in.koreatech.koin.admin.abtest.model.AbtestStatus;
import in.koreatech.koin.admin.abtest.model.AbtestVariable;
import in.koreatech.koin.admin.abtest.model.redis.AbtestVariableCount;
import in.koreatech.koin.admin.abtest.model.redis.AbtestVariableIp;
import in.koreatech.koin.admin.abtest.repository.AbtestRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableCountRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableIpRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableIpTemplateRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableRepository;
import in.koreatech.koin.admin.abtest.repository.AccessHistoryAbtestVariableCustomRepository;
import in.koreatech.koin.admin.abtest.repository.AccessHistoryAbtestVariableRepository;
import in.koreatech.koin.domain.user.model.AccessHistory;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.AccessHistoryRepository;
import in.koreatech.koin.domain.user.repository.DeviceRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.domain.notification.service.NotificationService;
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
    private final AbtestVariableIpRepository abtestVariableIpRepository;
    private final AbtestVariableIpTemplateRepository abtestVariableIpTemplateRepository;
    private final AccessHistoryAbtestVariableCustomRepository accessHistoryAbtestVariableCustomRepository;
    private final NotificationService notificationService;

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
        Abtest abtest = abtestRepository.getById(abtestId);
        validateAbtestInProgress(abtest);
        abtest.update(
            request.displayTitle(),
            request.creater(),
            request.team(),
            request.title(),
            request.description(),
            request.variables(),
            entityManager
        );
        syncCacheCountToDB(abtest);
        deleteVariableIpCache(abtest);
        modifyVariableByRate(abtest);
        return AbtestResponse.from(abtest);
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
            abtestVariableIpTemplateRepository.deleteByVariableId(abtestVariable.getId()));
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
    public void closeAbtest(Integer abtestId, AbtestCloseRequest request) {
        Abtest abtest = abtestRepository.getById(abtestId);
        validateAbtestInProgress(abtest);
        abtest.close(request.winnerName());
        syncCacheCountToDB(abtest);
        deleteCacheCount(abtest);
        deleteVariableIpCache(abtest);
    }

    @Transactional
    public String assignVariable(UserAgentInfo userAgentInfo, String ipAddress, Integer userId,
        AbtestAssignRequest request) {
        Abtest abtest = abtestRepository.getByTitle(request.title());
        Optional<String> winner = returnWinnerIfClosed(abtest);
        if (winner.isPresent()) {
            return winner.get();
        }
        validateAssignedUser(abtest, ipAddress);
        List<AbtestVariableCount> cacheCount = loadCacheCount(abtest);
        AbtestVariable variable = abtest.findAssignVariable(cacheCount);
        AccessHistory accessHistory = notificationService.findOrCreateAccessHistory(ipAddress);
        if (userRepository.findById(userId).isPresent()) {
            notificationService.createDeviceIfNotExists(userId, userAgentInfo, accessHistory);
        }
        accessHistory.addAbtestVariable(variable);
        countCacheUpdate(variable);
        variableIpCacheSave(variable, ipAddress);
        return variable.getName();
    }

    private List<AbtestVariableCount> loadCacheCount(Abtest abtest) {
        return abtest.getAbtestVariables().stream()
            .map(abtestVariable -> abtestVariableCountRepository.findOrCreateIfNotExists(abtestVariable.getId()))
            .toList();
    }

    private void countCacheUpdate(AbtestVariable variable) {
        AbtestVariableCount countCache = abtestVariableCountRepository.getById(variable.getId());
        countCache.addCount();
        abtestVariableCountRepository.save(countCache);
    }

    private void variableIpCacheSave(AbtestVariable variable, String ipAddress) {
        abtestVariableIpRepository.save(AbtestVariableIp.of(variable.getId(), ipAddress));
    }

    @Transactional
    public String getMyVariable(String title, String ipAddress) {
        Abtest abtest = abtestRepository.getByTitle(title);
        Optional<String> winner = returnWinnerIfClosed(abtest);
        if (winner.isPresent()) {
            return winner.get();
        }

        Optional<AbtestVariable> cacheVariable = abtest.getAbtestVariables().stream()
            .filter(abtestVariable ->
                abtestVariableIpRepository.findByVariableIdAndIp(abtestVariable.getId(), ipAddress).isPresent())
            .findAny();

        if (cacheVariable.isEmpty()) {
            AbtestVariable dbVariable = accessHistoryRepository.findByPublicIp(ipAddress)
                .orElseThrow(() -> AbtestNotAssignedUserException.withDetail("abtestId: " + abtest.getId() + ", "
                    + "publicIp: " + ipAddress))
                .findVariableByAbtestId(abtest.getId())
                .orElseThrow(() -> AbtestNotAssignedUserException.withDetail("abtestId: " + abtest.getId() + ", "
                    + "publicIp: " + ipAddress));
            abtestVariableIpRepository.save(AbtestVariableIp.of(dbVariable.getId(), ipAddress));
            return dbVariable.getName();
        }

        return cacheVariable.get().getName();
    }

    private void validateAssignedUser(Abtest abtest, String ipAddress) {
        Optional<AccessHistory> accessHistory = accessHistoryRepository.findByPublicIp(ipAddress);
        if (accessHistory.isEmpty()) {
            return;
        }
        if (abtest.getAbtestVariables().stream()
            .anyMatch(abtestVariable -> accessHistory.get().hasVariable(abtestVariable.getId()))) {
            throw AbtestAssignedUserException.withDetail("abtestId: " + abtest.getId() + ", publicIp: " + ipAddress);
        }
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

    @Transactional
    public void assignVariableByAdmin(Integer abtestId, AbtestAdminAssignRequest request) {
        Abtest abtest = abtestRepository.getById(abtestId);
        validateAbtestInProgress(abtest);
        AccessHistory accessHistory = accessHistoryRepository.getByDeviceId(request.deviceId());
        AbtestVariable beforeVariable = abtest.findVariableByAccessHistory(accessHistory);
        AbtestVariable afterVariable = abtest.getVariableByName(request.variableName());
        validateDuplicatedVariables(beforeVariable, afterVariable);
        abtest.assignVariableByAdmin(accessHistory, request.variableName());
        abtestVariableIpRepository.deleteByVariableIdAndIp(beforeVariable.getId(), accessHistory.getPublicIp());
        variableIpCacheSave(afterVariable, accessHistory.getPublicIp());
    }

    private static void validateDuplicatedVariables(AbtestVariable beforeVariable, AbtestVariable afterVariable) {
        if (Objects.equals(beforeVariable.getId(), afterVariable.getId())) {
            throw AbtestDuplicatedVariableException.withDetail("beforeVariable id: " + beforeVariable.getId()
                + ", afterVariable id: " + afterVariable.getId());
        }
    }

    private static void validateAbtestInProgress(Abtest abtest) {
        if (abtest.getStatus() != AbtestStatus.IN_PROGRESS) {
            throw AbtestNotInProgressException.withDetail("abtestId: " + abtest.getId());
        }
    }

    private static Optional<String> returnWinnerIfClosed(Abtest abtest) {
        if (abtest.getStatus() == AbtestStatus.CLOSED) {
            if (abtest.getWinner() != null) {
                return Optional.of(abtest.getWinnerName());
            }
            throw AbtestWinnerNotDecidedException.withDetail("abtestId: " + abtest.getId());
        }
        return Optional.empty();
    }
}
