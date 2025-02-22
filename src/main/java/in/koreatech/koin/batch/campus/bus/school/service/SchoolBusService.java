package in.koreatech.koin.batch.campus.bus.school.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.batch.campus.bus.school.dto.BatchSchoolBusVersionUpdateRequest;
import in.koreatech.koin.batch.campus.bus.school.dto.SchoolBusTimetable;
import in.koreatech.koin.batch.campus.bus.school.model.BusInfo;
import in.koreatech.koin.batch.campus.bus.school.model.BusType;
import in.koreatech.koin.batch.campus.bus.school.model.SchoolBus;
import in.koreatech.koin.batch.campus.bus.school.repository.SchoolBusRepository;
import in.koreatech.koin.batch.campus.util.YamlParser;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class SchoolBusService {

    private final VersionRepository versionRepository;
    private final SchoolBusRepository schoolBusRepository;
    private final YamlParser yamlParser;
    private final Clock clock;

    private static final Map<String, BusInfo> fileMapper = Map.of(
        "cheonan_commuting.yaml", new BusInfo("천안", BusType.COMMUTING),
        "cheonan_shuttle.yaml", new BusInfo("천안", BusType.SHUTTLE),
        "sejong_commuting.yaml", new BusInfo("세종", BusType.COMMUTING),
        "daejeon_commuting.yaml", new BusInfo("대전", BusType.COMMUTING),
        "seoul_commuting.yaml", new BusInfo("서울", BusType.COMMUTING),
        "cheongju_shuttle.yaml", new BusInfo("청주", BusType.SHUTTLE),
        "cheongju_commuting.yaml", new BusInfo("청주", BusType.COMMUTING)
    );

    public void update(BatchSchoolBusVersionUpdateRequest request) {
        String currentPath = System.getProperty("user.dir");

        fileMapper.forEach((fileName, busInfo) -> {
            try {
                SchoolBusTimetable timetable = yamlParser.parse(
                    Paths.get(currentPath, "yaml", fileName).toString(),
                    SchoolBusTimetable.class
                );

                updateTimetable(busInfo, timetable);

            } catch (IOException e) {
                throw new RuntimeException("error on reading: " + fileName, e);
            }
        });

        updateVersion(request.title(), request.content());
    }

    private void updateTimetable(BusInfo busInfo, SchoolBusTimetable timetable) {
        SchoolBus to = SchoolBus.builder()
            .region(busInfo.getRegion())
            .busType(busInfo.getBusType().getValue())
            .direction("to")
            .routes(timetable.getTo_school())
            .build();

        SchoolBus from = SchoolBus.builder()
            .region(busInfo.getRegion())
            .busType(busInfo.getBusType().getValue())
            .direction("from")
            .routes(timetable.getFrom_school())
            .build();

        schoolBusRepository.upsert(to);
        schoolBusRepository.upsert(from);
    }

    private void updateVersion(String title, String content) {
        Version version = versionRepository.findByType(VersionType.SHUTTLE.getValue())
            .orElseGet(() -> {
                Version newVersion = Version.of(VersionType.SHUTTLE, clock);
                return versionRepository.save(newVersion);
            });

        version.update(clock, title, content);
    }
}
