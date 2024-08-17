package in.koreatech.koin.admin.abtest.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.abtest.dto.AbtestAssignRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestRequest;
import in.koreatech.koin.admin.abtest.dto.AbtestResponse;
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
import in.koreatech.koin.admin.abtest.repository.AccessHistoryAbtestVariableRepository;
import in.koreatech.koin.admin.abtest.repository.AccessHistoryRepository;
import in.koreatech.koin.admin.abtest.repository.DeviceRepository;
import in.koreatech.koin.admin.abtest.repository.VariableIpRepository;
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
    private final VariableIpRepository variableIpRepository;
    private final AbtestRepository abtestRepository;
    private final AbtestVariableRepository abtestVariableRepository;
    private final AccessHistoryRepository accessHistoryRepository;
    private final AccessHistoryAbtestVariableRepository accessHistoryAbtestVariableRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

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
}
