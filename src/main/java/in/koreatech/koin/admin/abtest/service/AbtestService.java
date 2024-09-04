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
import in.koreatech.koin.admin.abtest.model.redis.AbtestVariableAssign;
import in.koreatech.koin.admin.abtest.model.redis.AbtestVariableCount;
import in.koreatech.koin.admin.abtest.repository.AbtestRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableAssignRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableAssignTemplateRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableCountRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableRepository;
import in.koreatech.koin.admin.abtest.repository.AccessHistoryAbtestVariableCustomRepository;
import in.koreatech.koin.admin.abtest.model.AccessHistory;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.admin.abtest.repository.AccessHistoryRepository;
import in.koreatech.koin.admin.abtest.repository.DeviceRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.service.UserService;
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
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final AbtestVariableAssignRepository abtestVariableAssignRepository;
    private final AbtestVariableAssignTemplateRepository abtestVariableAssignTemplateRepository;
    private final AccessHistoryAbtestVariableCustomRepository accessHistoryAbtestVariableCustomRepository;
    private final UserService userService;

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
                .creator(request.creator())
                .team(request.team())
                .status(AbtestStatus.IN_PROGRESS)
                .build()
        );
        List<AbtestVariable> abtestVariables = request.variables().stream()
            .map(variable -> AbtestVariable.builder()
                .displayName(variable.displayName())
                .name(variable.name())
                .rate(variable.rate())
                .abtest(saved)
                .build())
            .toList();
        saved.setVariables(abtestVariables, entityManager);
        return AbtestResponse.from(saved);
    }

    @Transactional
    public AbtestResponse putAbtest(Integer abtestId, AbtestRequest request) {
        Abtest abtest = abtestRepository.getById(abtestId);
        validateAbtestInProgress(abtest);
        // TODO: 실험 수정 시 기존 변수가 사라지면 map까지 전부 사라져서 편입된 사용자들이 지워지는 문제 고치기
        syncCacheCountToDB(abtest);
        deleteCountCache(abtest);
        deleteVariableIpCache(abtest);
        Abtest requestedAbtest = Abtest.builder()
            .title(request.title())
            .displayTitle(request.displayTitle())
            .description(request.description())
            .creator(request.creator())
            .team(request.team())
            .status(AbtestStatus.IN_PROGRESS)
            .build();
        List<AbtestVariable> requestedAbtestVariables = request.variables().stream()
            .map(variable -> AbtestVariable.builder()
                .abtest(requestedAbtest)
                .displayName(variable.displayName())
                .name(variable.name())
                .rate(variable.rate())
                .build())
            .toList();
        requestedAbtest.setVariables(requestedAbtestVariables, entityManager);
        modifyVariableByRate(abtest, requestedAbtest);
        abtest.update(requestedAbtest, entityManager);
        return AbtestResponse.from(abtest);
    }

    //TODO: 수정 필요, 고장나있음
    private void modifyVariableByRate(Abtest abtest, Abtest requestedAbtest) {
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
            abtestVariableAssignTemplateRepository.deleteAllByVariableId(abtestVariable.getId()));
    }

    @Transactional
    public void deleteAbtest(Integer abtestId) {
        abtestRepository.findById(abtestId).ifPresent(saved -> {
            syncCacheCountToDB(saved);
            deleteCountCache(saved);
            deleteVariableIpCache(saved);
            abtestRepository.deleteById(abtestId);
        });
    }

    private void deleteCountCache(Abtest abtest) {
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
        deleteCountCache(abtest);
        deleteVariableIpCache(abtest);
    }

    @Transactional
    public String assignVariable(UserAgentInfo userAgentInfo, Integer userId, AbtestAssignRequest request) {
        Abtest abtest = abtestRepository.getByTitle(request.title());
        Optional<String> winner = returnWinnerIfClosed(abtest);
        if (winner.isPresent()) {
            return winner.get();
        }
        validateAssignedUser(abtest, ipAddress, userId);
        List<AbtestVariableCount> cacheCount = loadCacheCount(abtest);
        AbtestVariable variable = abtest.findAssignVariable(cacheCount);
        AccessHistory accessHistory = userService.findOrCreateAccessHistory(ipAddress);
        if (userId != null && userRepository.findById(userId).isPresent()) {
            userService.createDeviceIfNotExists(userId, userAgentInfo, accessHistory);
        }
        removeBeforeUserCache(ipAddress, accessHistory, abtest);
        accessHistory.addAbtestVariable(variable);
        countCacheUpdate(variable);
        variableIpCacheSave(variable, ipAddress);
        return variable.getName();
    }

    // 기기를 다른 사용자가 사용한 이력이 있는 경우 기존 사용자의 캐시를 삭제
    private void removeBeforeUserCache(String ipAddress, AccessHistory accessHistory, Abtest abtest) {
        for (AbtestVariable removeVariable : accessHistory.getVariableBy(abtest)) {
            abtestVariableAssignRepository.deleteByVariableIdAndIp(removeVariable.getId(), ipAddress);
            AbtestVariableCount countCache = abtestVariableCountRepository.findOrCreateIfNotExists(
                removeVariable.getId());
            countCache.minusCount();
            abtestVariableCountRepository.save(countCache);
        }
    }

    private List<AbtestVariableCount> loadCacheCount(Abtest abtest) {
        return abtest.getAbtestVariables().stream()
            .map(abtestVariable -> abtestVariableCountRepository.findOrCreateIfNotExists(abtestVariable.getId()))
            .toList();
    }

    private void countCacheUpdate(AbtestVariable variable) {
        AbtestVariableCount countCache = abtestVariableCountRepository.findOrCreateIfNotExists(variable.getId());
        countCache.addCount();
        abtestVariableCountRepository.save(countCache);
    }

    private void variableIpCacheSave(AbtestVariable variable, String ipAddress) {
        abtestVariableAssignRepository.save(AbtestVariableAssign.of(variable.getId(), ipAddress));
    }

    @Transactional
    public String getMyVariable(String title, String ipAddress) {
        Abtest abtest = abtestRepository.getByTitle(title);
        syncCacheCountToDB(abtest);
        Optional<String> winner = returnWinnerIfClosed(abtest);
        if (winner.isPresent()) {
            return winner.get();
        }

        Optional<AbtestVariable> cacheVariable = abtest.getAbtestVariables().stream()
            .filter(abtestVariable ->
                abtestVariableAssignRepository.findByVariableIdAndIp(abtestVariable.getId(), ipAddress).isPresent())
            .findAny();

        if (cacheVariable.isEmpty()) {
            AbtestVariable dbVariable = accessHistoryRepository.findById(ipAddress)
                .orElseThrow(() -> AbtestNotAssignedUserException.withDetail("abtestId: " + abtest.getId() + ", "
                    + "publicIp: " + ipAddress))
                .findVariableByAbtestId(abtest.getId())
                .orElseThrow(() -> AbtestNotAssignedUserException.withDetail("abtestId: " + abtest.getId() + ", "
                    + "publicIp: " + ipAddress));
            abtestVariableAssignRepository.save(AbtestVariableAssign.of(dbVariable.getId(), ipAddress));
            return dbVariable.getName();
        }

        return cacheVariable.get().getName();
    }

    private void validateAssignedUser(Abtest abtest, String ipAddress, Integer userId) {
        Optional<AccessHistory> accessHistory = accessHistoryRepository.findById(ipAddress);
        if (accessHistory.isEmpty() || (userId != null && !Objects.equals(accessHistory.get().getDevice().getUser().getId(), userId))) {
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
        cacheCount.removeIf(Objects::isNull);
        cacheCount.forEach(abtestVariableCount -> {
            Optional<AbtestVariable> variable = abtestVariableRepository.findById(abtestVariableCount.getVariableId());
            if (variable.isEmpty()) {
                abtestVariableCountRepository.deleteById(abtestVariableCount.getVariableId());
                return;
            }
            variable.get().addCount(abtestVariableCount.getCount());
            abtestVariableCount.resetCount();
        });
        abtestVariableCountRepository.saveAll(cacheCount);
    }

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
        Optional<AbtestVariable> beforeVariable = abtest.findVariableByAccessHistory(accessHistory);
        AbtestVariable afterVariable = abtest.getVariableByName(request.variableName());
        validateDuplicatedVariables(beforeVariable, afterVariable);
        abtest.assignVariableByAdmin(accessHistory, request.variableName());
        beforeVariable.ifPresent(
            abtestVariable -> abtestVariableAssignRepository.deleteByVariableIdAndIp(abtestVariable.getId(),
                accessHistory.getPublicIp()));
        variableIpCacheSave(afterVariable, accessHistory.getPublicIp());
    }

    private static void validateDuplicatedVariables(Optional<AbtestVariable> beforeVariable,
        AbtestVariable afterVariable) {
        if (beforeVariable.isEmpty()) {
            return;
        }
        if (Objects.equals(beforeVariable.get().getId(), afterVariable.getId())) {
            throw AbtestDuplicatedVariableException.withDetail("beforeVariable id: " + beforeVariable.get().getId()
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
