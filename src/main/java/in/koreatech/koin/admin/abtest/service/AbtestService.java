package in.koreatech.koin.admin.abtest.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.abtest.dto.request.AbtestAdminAssignRequest;
import in.koreatech.koin.admin.abtest.dto.request.AbtestAssignRequest;
import in.koreatech.koin.admin.abtest.dto.request.AbtestCloseRequest;
import in.koreatech.koin.admin.abtest.dto.request.AbtestRequest;
import in.koreatech.koin.admin.abtest.dto.response.AbtestAccessHistoryResponse;
import in.koreatech.koin.admin.abtest.dto.response.AbtestAssignResponse;
import in.koreatech.koin.admin.abtest.dto.response.AbtestDevicesResponse;
import in.koreatech.koin.admin.abtest.dto.response.AbtestResponse;
import in.koreatech.koin.admin.abtest.dto.response.AbtestUsersResponse;
import in.koreatech.koin.admin.abtest.dto.response.AbtestsResponse;
import in.koreatech.koin.admin.abtest.exception.AbtestAlreadyExistException;
import in.koreatech.koin.admin.abtest.exception.AbtestDuplicatedVariableException;
import in.koreatech.koin.admin.abtest.exception.AbtestNotAssignedUserException;
import in.koreatech.koin.admin.abtest.exception.AbtestNotInProgressException;
import in.koreatech.koin.admin.abtest.exception.AbtestWinnerNotDecidedException;
import in.koreatech.koin.admin.abtest.model.Abtest;
import in.koreatech.koin.admin.abtest.model.AbtestStatus;
import in.koreatech.koin.admin.abtest.model.AbtestVariable;
import in.koreatech.koin.admin.abtest.model.AccessHistory;
import in.koreatech.koin.admin.abtest.model.Device;
import in.koreatech.koin.admin.abtest.model.redis.AbtestVariableAssign;
import in.koreatech.koin.admin.abtest.model.redis.AbtestVariableCount;
import in.koreatech.koin.admin.abtest.repository.AbtestRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableAssignRedisRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableAssignTemplateRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableCountRedisRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableRepository;
import in.koreatech.koin.admin.abtest.repository.AccessHistoryRepository;
import in.koreatech.koin.admin.abtest.repository.DeviceRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AbtestService {

    private final EntityManager entityManager;
    private final AbtestVariableCountRedisRepository abtestVariableCountRedisRepository;
    private final AbtestRepository abtestRepository;
    private final AbtestVariableRepository abtestVariableRepository;
    private final AccessHistoryRepository accessHistoryRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final AbtestVariableAssignRedisRepository abtestVariableAssignRedisRepository;
    private final AbtestVariableAssignTemplateRepository abtestVariableAssignTemplateRepository;

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
        saved.setVariables(request.variables(), entityManager);
        return AbtestResponse.from(saved);
    }

    @Transactional
    public AbtestResponse putAbtest(Integer abtestId, AbtestRequest request) {
        Abtest abtest = abtestRepository.getById(abtestId);
        validateAbtestInProgress(abtest);
        abtest.update(
            request.displayTitle(),
            request.creator(),
            request.team(),
            request.title(),
            request.description(),
            request.variables()
        );
        return AbtestResponse.from(abtest);
    }

    private void deleteVariableAssignCache(Abtest abtest) {
        abtest.getAbtestVariables().forEach(abtestVariable ->
            abtestVariableAssignTemplateRepository.deleteAllByVariableId(abtestVariable.getId()));
    }

    @Transactional
    public void deleteAbtest(Integer abtestId) {
        abtestRepository.findById(abtestId).ifPresent(saved -> {
            syncCacheCountToDB(saved);
            deleteCountCache(saved);
            deleteVariableAssignCache(saved);
            abtestRepository.deleteById(abtestId);
        });
    }

    private void deleteCountCache(Abtest abtest) {
        abtest.getAbtestVariables()
            .forEach(abtestVariable -> abtestVariableCountRedisRepository.deleteById(abtestVariable.getId()));
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
        deleteVariableAssignCache(abtest);
    }

    @Transactional
    public AbtestAssignResponse assignOrGetVariable(Integer accessHistoryId, UserAgentInfo userAgentInfo,
        Integer userId, AbtestAssignRequest request) {
        Abtest abtest = abtestRepository.getByTitle(request.title());
        AccessHistory accessHistory = findOrCreateAccessHistory(accessHistoryId);
        Optional<AbtestVariable> winnerResponse = returnWinnerIfClosed(abtest);
        if (winnerResponse.isPresent()) {
            return AbtestAssignResponse.of(winnerResponse.get(), accessHistory);
        }
        if (isAssignedUser(abtest, accessHistory.getId(), userId)) {
            return AbtestAssignResponse.of(
                getMyVariable(accessHistory.getId(), userAgentInfo, userId, request.title()), accessHistory);
        }
        List<AbtestVariableCount> cacheCount = loadCacheCount(abtest);
        AbtestVariable variable = abtest.findAssignVariable(cacheCount);
        if (userId != null) {
            createDeviceIfNotExists(userId, userAgentInfo, accessHistory, abtest);
        }
        accessHistory.addAbtestVariable(variable);
        countCacheUpdate(variable);
        variableAssignCacheSave(variable, accessHistory.getId());
        return AbtestAssignResponse.of(variable, accessHistory);
    }

    // 기기를 다른 사용자가 사용한 이력이 있는 경우 기존 사용자의 캐시를 삭제
    private void removeBeforeUserCache(AccessHistory accessHistory, Abtest abtest) {
        for (AbtestVariable removeVariable : accessHistory.getVariableBy(abtest)) {
            abtestVariableAssignRedisRepository.deleteByVariableIdAndAccessHistoryId(removeVariable.getId(),
                accessHistory.getId());
            AbtestVariableCount countCache = abtestVariableCountRedisRepository.findOrCreateIfNotExists(
                removeVariable.getId());
            countCache.minusCount();
            abtestVariableCountRedisRepository.save(countCache);
        }
    }

    private List<AbtestVariableCount> loadCacheCount(Abtest abtest) {
        return abtest.getAbtestVariables().stream()
            .map(abtestVariable -> abtestVariableCountRedisRepository.findOrCreateIfNotExists(abtestVariable.getId()))
            .toList();
    }

    private void countCacheUpdate(AbtestVariable variable) {
        AbtestVariableCount countCache = abtestVariableCountRedisRepository.findOrCreateIfNotExists(variable.getId());
        countCache.addCount();
        abtestVariableCountRedisRepository.save(countCache);
    }

    private void variableAssignCacheSave(AbtestVariable variable, Integer accessHistoryId) {
        abtestVariableAssignRedisRepository.save(AbtestVariableAssign.of(variable.getId(), accessHistoryId));
    }

    private AbtestVariable getMyVariable(Integer accessHistoryId, UserAgentInfo userAgentInfo, Integer userId,
        String title) {
        Abtest abtest = abtestRepository.getByTitle(title);
        AccessHistory accessHistory = accessHistoryRepository.getById(accessHistoryId);
        syncCacheCountToDB(abtest);
        Optional<AbtestVariable> winner = returnWinnerIfClosed(abtest);
        if (winner.isPresent()) {
            return winner.get();
        }
        if (userId != null) {
            createDeviceIfNotExists(userId, userAgentInfo, accessHistory, abtest);
        }
        Optional<AbtestVariable> cacheVariable = abtest.getAbtestVariables().stream()
            .filter(abtestVariable ->
                abtestVariableAssignRedisRepository.findByVariableIdAndAccessHistoryId(abtestVariable.getId(),
                    accessHistory.getId()).isPresent())
            .findAny();
        if (cacheVariable.isEmpty()) {
            AbtestVariable dbVariable = accessHistory.findVariableByAbtestId(abtest.getId())
                .orElseThrow(() -> AbtestNotAssignedUserException.withDetail("abtestId: " + abtest.getId() + ", "
                    + "accessHistoryId: " + accessHistory.getId()));
            abtestVariableAssignRedisRepository.save(AbtestVariableAssign.of(dbVariable.getId(), accessHistory.getId()));
            return dbVariable;
        }
        return cacheVariable.get();
    }

    private boolean isAssignedUser(Abtest abtest, Integer accessHistoryId, Integer userId) {
        AccessHistory accessHistory = accessHistoryRepository.getById(accessHistoryId);
        if (userId != null && accessHistory.getDevice() != null
            && !Objects.equals(accessHistory.getDevice().getUser().getId(), userId)) {
            return false;
        }
        return abtest.getAbtestVariables().stream()
            .anyMatch(abtestVariable -> accessHistory.hasVariable(abtestVariable.getId()));
    }

    @Transactional
    public void syncCacheCountToDB() {
        List<AbtestVariableCount> cacheCount = abtestVariableCountRedisRepository.findAll();
        cacheCount.removeIf(Objects::isNull);
        cacheCount.forEach(abtestVariableCount -> {
            Optional<AbtestVariable> variable = abtestVariableRepository.findById(abtestVariableCount.getVariableId());
            if (variable.isEmpty()) {
                abtestVariableCountRedisRepository.deleteById(abtestVariableCount.getVariableId());
                return;
            }
            variable.get().addCount(abtestVariableCount.getCount());
            abtestVariableCount.resetCount();
        });
        abtestVariableCountRedisRepository.saveAll(cacheCount);
    }

    public void syncCacheCountToDB(Abtest abtest) {
        List<AbtestVariableCount> cacheCount = abtest.getAbtestVariables().stream()
            .map(AbtestVariable::getId)
            .map(abtestVariableCountRedisRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
        cacheCount.forEach(abtestVariableCount -> {
            AbtestVariable variable = abtestVariableRepository.getById(abtestVariableCount.getVariableId());
            variable.addCount(abtestVariableCount.getCount());
            abtestVariableCount.resetCount();
        });
        abtestVariableCountRedisRepository.saveAll(cacheCount);
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
            abtestVariable -> abtestVariableAssignRedisRepository.deleteByVariableIdAndAccessHistoryId(
                abtestVariable.getId(),
                accessHistory.getId()));
        variableAssignCacheSave(afterVariable, accessHistory.getId());
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

    private static Optional<AbtestVariable> returnWinnerIfClosed(Abtest abtest) {
        if (abtest.getStatus() == AbtestStatus.CLOSED) {
            if (abtest.getWinner() != null) {
                return Optional.of(abtest.getWinner());
            }
            throw AbtestWinnerNotDecidedException.withDetail("abtestId: " + abtest.getId());
        }
        return Optional.empty();
    }

    public AccessHistory findOrCreateAccessHistory(Integer id) {
        if (id == null) {
            return accessHistoryRepository.save(AccessHistory.create());
        }
        return accessHistoryRepository.getById(id);
    }

    @Transactional
    public AbtestAccessHistoryResponse issueAccessHistoryId(UserAgentInfo userAgentInfo, Integer userId) {
        AccessHistory accessHistory = accessHistoryRepository.save(AccessHistory.create());
        if (userId != null) {
            createDeviceIfNotExists(userId, userAgentInfo, accessHistory, null);
        }
        return AbtestAccessHistoryResponse.from(accessHistory);
    }

    private void createDeviceIfNotExists(Integer userId, UserAgentInfo userAgentInfo,
        AccessHistory accessHistory, Abtest abtest) {
        User user = userRepository.getById(userId);
        Device device = accessHistory.getDevice();
        if (device == null) {
            device = deviceRepository.save(
                Device.builder()
                    .user(user)
                    .model(userAgentInfo.getModel())
                    .type(userAgentInfo.getType())
                    .build()
            );
            accessHistory.connectDevice(device);
        }
        if (device.getModel() == null || device.getType() == null) {
            device.setModelInfo(userAgentInfo.getModel(), userAgentInfo.getType());
        }
        if (!Objects.equals(device.getUser().getId(), userId)) {
            device.changeUser(user);
            if (abtest != null) {
                removeBeforeUserCache(accessHistory, abtest);
            }
        }
    }
}
