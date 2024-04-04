package in.koreatech.koin.domain.bus.util;

import static in.koreatech.koin.domain.bus.model.enums.BusType.EXPRESS;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;

import java.lang.reflect.Type;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import in.koreatech.koin.domain.bus.dto.ExpressBusRemainTime;
import in.koreatech.koin.domain.bus.dto.ExpressBusRemainTime.InnerRemainTime;
import in.koreatech.koin.domain.bus.dto.ExpressBusTimeTable;
import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.enums.BusOpenApiResultCode;
import in.koreatech.koin.domain.bus.model.express.ExpressBusArrival;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCache;
import in.koreatech.koin.domain.bus.model.express.ExpressBusCacheInfo;
import in.koreatech.koin.domain.bus.model.express.ExpressBusRoute;
import in.koreatech.koin.domain.bus.model.express.ExpressBusStationNode;
import in.koreatech.koin.domain.bus.repository.ExpressBusCacheRepository;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;

/**
 * OpenApi 상세: 국토교통부_(TAGO)_버스도착정보
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15098541
 */
@Component
@Transactional(readOnly = true)
public class ExpressBusOpenApiClient extends BusOpenApiClient<BusRemainTime> {

    private static final String OPEN_API_URL = "https://apis.data.go.kr/1613000/SuburbsBusInfoService/getStrtpntAlocFndSuberbsBusInfo";
    private static final Type ARRIVAL_INFO_TYPE = new TypeToken<List<ExpressBusArrival>>() {
    }.getType();

    private final RestTemplate restTemplate;
    private final VersionRepository versionRepository;
    private final ExpressBusCacheRepository expressBusCacheRepository;
    private final String openApiKey;
    private final Gson gson;
    private final Clock clock;

    public ExpressBusOpenApiClient(
        @Value("${OPEN_API_KEY}") String openApiKey,
        VersionRepository versionRepository,
        Gson gson,
        Clock clock,
        ExpressBusCacheRepository expressBusCacheRepository,
        RestTemplate restTemplate
    ) {
        this.openApiKey = openApiKey;
        this.versionRepository = versionRepository;
        this.gson = gson;
        this.clock = clock;
        this.expressBusCacheRepository = expressBusCacheRepository;
        this.restTemplate = restTemplate;
    }

    public ExpressBusRemainTime getBusRemainTime(String departName, String arrivalName) {
        Version version = versionRepository.getByType(VersionType.EXPRESS);
        if (isCacheExpired(version, clock)) {
            storeRemainTimeByOpenApi(departName, arrivalName);
        }
        return getStoredRemainTime(departName, arrivalName);
    }

    private ExpressBusRemainTime storeRemainTimeByOpenApi(String departName, String arrivalName) {
        JsonObject busApiResponse = getBusApiResponse(departName, arrivalName);
        List<ExpressBusArrival> busArrivals = extractBusArrivalInfo(busApiResponse);
        expressBusCacheRepository.save(
            ExpressBusCache.create(
                new ExpressBusRoute(departName, arrivalName),
                busArrivals.stream()
                    .map(it -> new ExpressBusCacheInfo(
                        LocalTime.parse(
                            LocalDateTime.parse(it.depPlandTime(), ofPattern("yyyyMMddHHmm"))
                                .format(ofPattern("HH:mm"))
                        ),
                        LocalTime.parse(
                            LocalDateTime.parse(it.arrPlandTime(), ofPattern("yyyyMMddHHmm"))
                                .format(ofPattern("HH:mm"))
                        ),
                        it.charge()
                    ))
                    .toList()
            ));
        versionRepository.getByType(VersionType.EXPRESS).update(clock);
        var now = LocalDateTime.now(clock);
        return getExpressBusRemainTime(
            busArrivals
                .stream()
                .map(ExpressBusTimeTable::from),
            now
        );
    }

    private JsonObject getBusApiResponse(String departName, String arrivalName) {
        ExpressBusStationNode departNode = ExpressBusStationNode.from(departName);
        ExpressBusStationNode arrivalNode = ExpressBusStationNode.from(arrivalName);
        String contentCount = "30";
        var parameters = Map.of("serviceKey", openApiKey,
            "numOfRows", contentCount,
            "depTerminalName", departNode.getStationId(),
            "arrTerminalName", arrivalNode.getStationId(),
            "depPlandTime", LocalDateTime.now(clock).format(ofPattern("yyyyMMdd")),
            "_type", "json"
        );
        ResponseEntity<String> forEntity = restTemplate.getForEntity(OPEN_API_URL, String.class, parameters);
        return JsonParser.parseString(Objects.requireNonNull(forEntity.getBody()))
            .getAsJsonObject();
    }

    private List<ExpressBusArrival> extractBusArrivalInfo(JsonObject jsonObject) {
        try {
            var response = jsonObject.get("response").getAsJsonObject();
            BusOpenApiResultCode.validateResponse(response);
            JsonObject body = response.get("body").getAsJsonObject();
            if (body.get("totalCount").getAsLong() == 0) {
                return Collections.emptyList();
            }
            JsonElement item = body.get("items").getAsJsonObject().get("item");
            List<ExpressBusArrival> result = new ArrayList<>();
            if (item.isJsonArray()) {
                return gson.fromJson(item, ARRIVAL_INFO_TYPE);
            }
            if (item.isJsonObject()) {
                result.add(gson.fromJson(item, ExpressBusArrival.class));
            }
            return result;
        } catch (JsonSyntaxException e) {
            return Collections.emptyList();
        }
    }

    private ExpressBusRemainTime getStoredRemainTime(String departName, String arrivalName) {
        String busCacheId = ExpressBusCache.generateId(new ExpressBusRoute(departName, arrivalName));
        ExpressBusCache expressBusCache = expressBusCacheRepository.getById(busCacheId);
        if (Objects.isNull(expressBusCache)) {
            return ExpressBusRemainTime.builder()
                .busType(EXPRESS.name())
                .build();
        }
        List<ExpressBusCacheInfo> busArrivals = expressBusCache.getBusInfos();
        LocalDateTime now = LocalDateTime.now(clock);

        return getExpressBusRemainTime(
            busArrivals
                .stream()
                .map(ExpressBusTimeTable::from)
            , now);
    }

    private ExpressBusRemainTime getExpressBusRemainTime(
        Stream<ExpressBusTimeTable> busArrivals,
        LocalDateTime now
    ) {
        List<ExpressBusTimeTable> busTimes = busArrivals.toList();
        List<LocalTime> closestFutureTimes = findClosestFutureTimes(LocalDateTime.now(clock), busTimes);
        if (closestFutureTimes.isEmpty()) {
            return
                ExpressBusRemainTime.builder()
                    .busType(EXPRESS.name())
                    .build();
        } else if (closestFutureTimes.size() == 1) {
            return
                ExpressBusRemainTime.builder()
                    .busType(EXPRESS.name())
                    .nowBus(new InnerRemainTime(null, SECONDS.between(now, closestFutureTimes.get(0))))
                    .nextBus(null)
                    .build();
        }

        LocalTime nowDepartureTime = closestFutureTimes.get(0);
        LocalTime nextDepartureTime = closestFutureTimes.get(1);

        return ExpressBusRemainTime.builder()
            .busType(EXPRESS.name())
            .nowBus(new InnerRemainTime(null, SECONDS.between(now, nowDepartureTime)))
            .nextBus(new InnerRemainTime(null, SECONDS.between(now, nextDepartureTime)))
            .build();
    }

    private List<LocalTime> findClosestFutureTimes(LocalDateTime now, List<ExpressBusTimeTable> arrivals) {
        // 현재 시간 이후의 시간만 필터링
        List<LocalTime> futureTimes = arrivals.stream()
            .map(ExpressBusTimeTable::departure)
            .filter(it -> it.isAfter(LocalTime.from(now)))
            // 필터링된 시간을 현재 시간과의 차이(절대값)에 따라 정렬
            .sorted(Comparator.comparingLong(time -> MILLIS.between(now, time)))
            .toList();

        // 결과 리스트의 크기가 2보다 작을 수 있으므로, 실제 크기와 2 중 더 작은 값을 상한으로 사용
        return futureTimes.subList(0, 2);
    }

    @Override
    public boolean isCacheExpired(Version version, Clock clock) {
        Duration duration = Duration.between(version.getUpdatedAt().toLocalTime(), LocalTime.now(clock));
        return duration.toSeconds() < 0 || ExpressBusCache.getCacheExpireHour() <= duration.toHours();
    }
}
