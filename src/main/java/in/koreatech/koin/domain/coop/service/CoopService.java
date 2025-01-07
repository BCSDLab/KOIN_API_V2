package in.koreatech.koin.domain.coop.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.coop.dto.CoopLoginRequest;
import in.koreatech.koin.domain.coop.dto.CoopLoginResponse;
import in.koreatech.koin.domain.coop.dto.DiningImageRequest;
import in.koreatech.koin.domain.coop.dto.SoldOutRequest;
import in.koreatech.koin.domain.coop.exception.DiningLimitDateException;
import in.koreatech.koin.domain.coop.exception.DiningNowDateException;
import in.koreatech.koin.domain.coop.exception.DuplicateExcelRequestException;
import in.koreatech.koin.domain.coop.exception.StartDateAfterEndDateException;
import in.koreatech.koin.domain.coop.model.Coop;
import in.koreatech.koin.domain.coop.model.DiningImageUploadEvent;
import in.koreatech.koin.domain.coop.model.DiningSoldOutEvent;
import in.koreatech.koin.domain.coop.model.ExcelDownloadCache;
import in.koreatech.koin.domain.coop.repository.CoopRepository;
import in.koreatech.koin.domain.coop.repository.DiningNotifyCacheRepository;
import in.koreatech.koin.domain.coop.repository.DiningSoldOutCacheRepository;
import in.koreatech.koin.domain.coop.repository.ExcelDownloadCacheRepository;
import in.koreatech.koin.domain.coopshop.model.CoopShopType;
import in.koreatech.koin.domain.coopshop.service.CoopShopService;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.repository.UserTokenRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoopService {

    private final Clock clock;
    private final ApplicationEventPublisher eventPublisher;
    private final DiningRepository diningRepository;
    private final DiningSoldOutCacheRepository diningSoldOutCacheRepository;
    private final ExcelDownloadCacheRepository excelDownloadCacheRepository;
    private final DiningNotifyCacheRepository diningNotifyCacheRepository;
    private final CoopRepository coopRepository;
    private final UserTokenRepository userTokenRepository;
    private final CoopShopService coopShopService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final List<String> placeFilters = Arrays.asList("A코너", "B코너", "C코너");

    public static final LocalDate LIMIT_DATE = LocalDate.of(2022, 11, 29);
    private final int EXCEL_COLUMN_COUNT = 8;

    @Transactional
    public void changeSoldOut(SoldOutRequest soldOutRequest) {
        Dining dining = diningRepository.getById(soldOutRequest.menuId());

        if (soldOutRequest.soldOut()) {
            LocalDateTime now = LocalDateTime.now(clock);
            dining.setSoldOut(now);
            boolean isOpened = coopShopService.getIsOpened(now, CoopShopType.CAFETERIA, dining.getType(), false);
            if (isOpened && diningSoldOutCacheRepository.findById(dining.getPlace()).isEmpty()) {
                eventPublisher.publishEvent(
                    new DiningSoldOutEvent(dining.getId(), dining.getPlace(), dining.getType()));
            }
        } else {
            dining.cancelSoldOut();
        }
    }

    @Transactional
    public void saveDiningImage(DiningImageRequest imageRequest) {
        Dining dining = diningRepository.getById(imageRequest.menuId());

        boolean isImageExist = diningRepository.existsByDateAndTypeAndImageUrlIsNotNull(dining.getDate(),
            dining.getType());
        LocalDateTime now = LocalDateTime.now(clock);
        boolean isOpened = coopShopService.getIsOpened(now, CoopShopType.CAFETERIA, dining.getType(), true);
        if (isOpened && !isImageExist) {
            eventPublisher.publishEvent(new DiningImageUploadEvent(dining.getId(), dining.getImageUrl()));
        }

        dining.setImageUrl(imageRequest.imageUrl());
    }

    /* TODO: 알림 로직 테스트 후 주석 제거
    public void sendDiningNotify() {
        DiningType diningType = coopShopService.getDiningType();
        LocalDate nowDate = LocalDate.now(clock);
        List<Dining> dinings = diningRepository.findAllByDateAndType(nowDate, diningType);

        if (dinings.isEmpty()) {
            return;
        }

        boolean allImageExist = diningRepository.allExistsByDateAndTypeAndPlacesAndImageUrlIsNotNull(
            nowDate, diningType, placeFilters
        );

        boolean isOpened = coopShopService.getIsOpened(
            LocalDateTime.now(clock), CoopShopType.CAFETERIA, diningType, true
        );

        String diningNotifyId = nowDate.toString() + diningType;

        if (isOpened && allImageExist) {
            if (alreadyNotify(diningNotifyId))
                return;

            if (!diningNotifyCacheRepository.existsById(diningNotifyId)) {
                sendNotify(diningNotifyId, dinings);
            }
        }

        if (LocalTime.now().isAfter(diningType.getStartTime().minusMinutes(10))
            && LocalTime.now().isBefore(diningType.getStartTime())
            && !diningNotifyCacheRepository.existsById(diningNotifyId)
            && diningRepository.existsByDateAndTypeAndImageUrlIsNotNull(nowDate, diningType)
        ) {
            sendNotify(diningNotifyId, dinings);
        }
    }

    private boolean alreadyNotify(String diningNotifyId) {
        if (diningNotifyCacheRepository.existsById(diningNotifyId)) {
            return true;
        }
        return false;
    }

    private void sendNotify(String diningNotifyId, List<Dining> dinings) {
        diningNotifyCacheRepository.save(DiningNotifyCache.from(diningNotifyId));
        eventPublisher.publishEvent(new DiningImageUploadEvent(dinings.get(0).getId(), dinings.get(0).getImageUrl()));
    }*/

    @Transactional
    public CoopLoginResponse coopLogin(CoopLoginRequest request) {
        Coop coop = coopRepository.getByCoopId(request.id());
        User user = coop.getUser();

        if (!user.isSamePassword(passwordEncoder, request.password())) {
            throw new KoinIllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        String accessToken = jwtProvider.createToken(user);
        String refreshToken = String.format("%s-%d", UUID.randomUUID(), user.getId());
        UserToken savedToken = userTokenRepository.save(UserToken.create(user.getId(), refreshToken));
        user.updateLastLoggedTime(LocalDateTime.now());

        return CoopLoginResponse.of(accessToken, savedToken.getRefreshToken());
    }

    public ByteArrayInputStream generateDiningExcel(LocalDate startDate, LocalDate endDate, Boolean isCafeteria) {
        checkDuplicateExcelRequest(startDate, endDate);
        validateDates(startDate, endDate);
        List<Dining> dinings = fetchDiningData(startDate, endDate, isCafeteria);

        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            SXSSFSheet sheet = createSheet(workbook, "식단 메뉴");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle commonStyle = createCommonStyle(workbook);

            addHeaderRow(sheet, headerStyle);
            addDiningDataToSheet(dinings, sheet, commonStyle);

            return writeWorkbookToStream(workbook);
        } catch (IOException e) {
            throw new RuntimeException("엑셀 파일 생성 중 오류가 발생했습니다.", e);
        }
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();

        if (startDate.isBefore(LIMIT_DATE) || endDate.isBefore(LIMIT_DATE)) {
            throw new DiningLimitDateException("2022/11/29 식단부터 다운받을 수 있어요.");
        }
        if (startDate.isAfter(today) || endDate.isAfter(today)) {
            throw new DiningNowDateException("오늘 날짜 이후 기간은 설정할 수 없어요.");
        }
        if (startDate.isAfter(endDate)) {
            throw new StartDateAfterEndDateException("시작일은 종료일 이전으로 설정해주세요.");
        }
    }

    private List<Dining> fetchDiningData(LocalDate startDate, LocalDate endDate, Boolean isCafeteria) {
        if (isCafeteria) {
            List<String> cafeteriaPlaces = Arrays.asList("A코너", "B코너", "C코너");
            return diningRepository.findByDateBetweenAndPlaceIn(startDate, endDate, cafeteriaPlaces);
        }
        return diningRepository.findByDateBetween(startDate, endDate);
    }

    private SXSSFSheet createSheet(SXSSFWorkbook workbook, String sheetName) {
        SXSSFSheet sheet = workbook.createSheet(sheetName);
        sheet.setRandomAccessWindowSize(100);
        return sheet;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createCommonStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private void addHeaderRow(Sheet sheet, CellStyle headerStyle) {
        String[] headers = {"날짜", "타입", "코너", "칼로리", "메뉴", "이미지", "품절 여부", "변경 여부"};
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void addDiningDataToSheet(List<Dining> dinings, SXSSFSheet sheet, CellStyle commonStyle) {
        AtomicInteger rowIndex = new AtomicInteger(1);
        dinings.forEach(dining -> {
            Row row = sheet.createRow(rowIndex.getAndIncrement());
            fillDiningRow(dining, row, commonStyle);
        });

        for (int i = 0; i < EXCEL_COLUMN_COUNT; i++) {
            sheet.setColumnWidth(i, 6000);
        }
    }

    private void fillDiningRow(Dining dining, Row row, CellStyle commonStyle) {
        row.createCell(0).setCellValue(dining.getDate().toString());
        row.createCell(1).setCellValue(dining.getType().getDiningName());
        row.createCell(2).setCellValue(dining.getPlace());
        row.createCell(3).setCellValue(Optional.ofNullable(dining.getKcal()).orElse(0));
        row.createCell(4).setCellValue(formatMenu(dining.getMenu()));
        row.createCell(5).setCellValue(dining.getImageUrl());
        row.createCell(6).setCellValue(Optional.ofNullable(dining.getSoldOut()).map(Object::toString).orElse(""));
        row.createCell(7).setCellValue(Optional.ofNullable(dining.getIsChanged()).map(Object::toString).orElse(""));

        for (int i = 0; i < EXCEL_COLUMN_COUNT; i++) {
            row.getCell(i).setCellStyle(commonStyle);
        }
    }

    private String formatMenu(List<String> menu) {
        return String.join("\n", menu);
    }

    private ByteArrayInputStream writeWorkbookToStream(SXSSFWorkbook workbook) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            workbook.dispose();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private void checkDuplicateExcelRequest(LocalDate startDate, LocalDate endDate) {
        boolean isCacheExist = excelDownloadCacheRepository.existsById(startDate.toString() + endDate.toString());

        if (isCacheExist) {
            throw DuplicateExcelRequestException.withDetail(startDate, endDate);
        }
        excelDownloadCacheRepository.save(ExcelDownloadCache.from(startDate, endDate));
    }
}
