package in.koreatech.koin.domain.coop.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
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
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import in.koreatech.koin.domain.dining.model.enums.ExcelDiningPosition;
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
        checkDuplicateExcelRequest(startDate, endDate);     // 중복 요청 검사
        validateDates(startDate, endDate);                  // 날짜 유효성 검사
        List<Dining> dinings = fetchDiningData(startDate, endDate, isCafeteria);        // 식단 뽑기

        Map<LocalDate, List<Dining>> dateDinings = dinings.stream()
            .collect(Collectors.groupingBy(
                Dining::getDate, // 그룹화 기준
                TreeMap::new,    // 정렬된 Map 생성
                Collectors.toList() // 그룹화된 값 리스트화
            ));

        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            //SXSSFSheet sheet = createSheet(workbook, "식단 메뉴");
            ByteArrayInputStream excelDesign = createDiningExcelWithDetailedFormat(dateDinings, isCafeteria);

            return excelDesign;
        } catch (IOException e) {
            throw new RuntimeException("엑셀 파일 생성 중 오류가 발생했습니다.", e);
        }
    }

    private ByteArrayInputStream createDiningExcelWithDetailedFormat(
        Map<LocalDate, List<Dining>> dateDinings, Boolean isCafeteria
    ) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("식단");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle commonStyle = createCommonStyle(workbook);
        CellStyle cornerStyle = createCornerStyle(workbook);

        List<String> meals = List.of("조식", "중식", "석식");
        List<String> corners;

        if (isCafeteria) {
            corners = List.of("A코너", "B코너", "C코너");
        } else {
            corners = List.of("A코너", "B코너", "C코너", "능수관", "2캠퍼스");
        }

        // **1. 초기 행 생성 및 캐싱**
        Map<Integer, Row> rowCache = new HashMap<>();
        for (int i = 0; i < 1000; i++) { // 필요할 만큼의 행을 미리 생성
            rowCache.put(i, sheet.createRow(i));
        }

        int colIndex = 2; // 데이터를 작성할 첫 번째 열 (예: 2열)

        // **2. 날짜별 데이터 작성**
        for (Map.Entry<LocalDate, List<Dining>> entry : dateDinings.entrySet()) {
            LocalDate date = entry.getKey();
            List<Dining> dinings = entry.getValue();
            //int rowIndex1 = 1; // 데이터를 작성할 첫 번째 행 (1행부터 시작)

            // 날짜 헤더 추가 (0행 고정)
            Row dateRow = rowCache.get(0); // 0행을 캐시에서 가져옴
            Cell dateCell = dateRow.createCell(colIndex);
            dateCell.setCellValue(date.toString());
            dateCell.setCellStyle(headerStyle);

            // Dining 데이터를 작성
            for (Dining dining : dinings) {
                ExcelDiningPosition position = ExcelDiningPosition.from(dining.getType(), dining.getPlace());
                int startPosition = isCafeteria
                    ? position.getStartPositionOnlyCafeteria()
                    : position.getStartPositionAllPlace();

                if (placeFilters.contains(dining.getPlace())) {
                    drawExcelCafeteria(dining, rowCache, startPosition, colIndex, commonStyle);
                } else {
                    drawExcelNoneCafeteria(dining, rowCache, startPosition, colIndex, commonStyle);
                }
            }

            // 열 너비 설정
            sheet.setColumnWidth(colIndex, 6000); // 고정된 열 너비
            colIndex++; // 다음 날짜로 이동
        }

        int rowIndex = 1; // 코너 정보 추가를 위한 초기 행 인덱스
        for (String meal : meals) {
            int mealStartRow = rowIndex;

            for (String corner : corners) {
                int cornerStartRow = rowIndex;
                if (placeFilters.contains(corner)) {
                    rowIndex += 4;
                } else
                    rowIndex += 2;

                sheet.addMergedRegion(new CellRangeAddress(cornerStartRow, rowIndex - 1, 1, 1));
                int startRow = cornerStartRow;
                int endRow = rowIndex - 1;
                int columnIndex = 1;

                for (int row = startRow; row <= endRow; row++) {
                    Row currentRow = rowCache.get(row);
                    if (currentRow == null) {
                        currentRow = sheet.createRow(row);
                        rowCache.put(row, currentRow);
                    }
                    Cell cell = currentRow.createCell(columnIndex);
                    cell.setCellValue(corner); // 병합된 셀 값 설정
                    cell.setCellStyle(cornerStyle);
                }
            }

            Row mealRow = rowCache.get(mealStartRow); // 캐시된 행 가져오기
            Cell mealCell = mealRow.createCell(0);
            mealCell.setCellValue(meal);
            mealCell.setCellStyle(cornerStyle);

            sheet.addMergedRegion(new CellRangeAddress(mealStartRow, rowIndex - 1, 0, 0));
        }

        // 열 너비 설정
        sheet.setColumnWidth(0, 4000); // 식사
        sheet.setColumnWidth(1, 4000); // 코너

        // **3. 엑셀 데이터를 메모리에 저장**
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("엑셀 데이터 생성 중 오류 발생", e);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                throw new RuntimeException("워크북 닫기 중 오류 발생", e);
            }
        }
    }

    private void drawExcelNoneCafeteria(Dining dining, Map<Integer, Row> rowCache, int startPosition, int colIndex,
        CellStyle cellStyle) {
        Row menuRow = rowCache.get(startPosition); // 캐시된 행 가져오기
        Cell menuCell = menuRow.createCell(colIndex);
        menuCell.setCellValue(formatMenu(dining.getMenu())); // 메뉴 추가
        menuCell.setCellStyle(cellStyle);

        Row kcalRow = rowCache.get(startPosition + 1); // 캐시된 행 가져오기
        Cell kcalCell = kcalRow.createCell(colIndex);
        kcalCell.setCellValue(dining.getKcal() == null ? "0 kcal" : dining.getKcal() + " kcal");
        kcalCell.setCellStyle(cellStyle);
    }

    private void drawExcelCafeteria(Dining dining, Map<Integer, Row> rowCache, int startPosition, int colIndex,
        CellStyle cellStyle) {
        Row menuRow = rowCache.get(startPosition); // 캐시된 행 가져오기
        Cell menuCell = menuRow.createCell(colIndex);
        menuCell.setCellValue(formatMenu(dining.getMenu())); // 메뉴 추가
        menuCell.setCellStyle(cellStyle);

        Row imageRow = rowCache.get(startPosition + 1); // 캐시된 행 가져오기
        Cell imageCell = imageRow.createCell(colIndex);
        imageCell.setCellValue(dining.getImageUrl()); // URL 추가
        imageCell.setCellStyle(cellStyle);

        Row kcalRow = rowCache.get(startPosition + 2); // 캐시된 행 가져오기
        Cell kcalCell = kcalRow.createCell(colIndex);
        kcalCell.setCellValue(dining.getKcal() == null ? "0 kcal" : dining.getKcal() + " kcal");
        kcalCell.setCellStyle(cellStyle);

        Row soldOutRow = rowCache.get(startPosition + 3); // 캐시된 행 가져오기
        Cell soldOutCell = soldOutRow.createCell(colIndex);
        soldOutCell.setCellValue(dining.getSoldOut() == null ? "미품절" : "품절 (" + dining.getSoldOut() + ")");
        soldOutCell.setCellStyle(cellStyle);
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

    private void checkDuplicateExcelRequest(LocalDate startDate, LocalDate endDate) {
        boolean isCacheExist = excelDownloadCacheRepository.existsById(startDate.toString() + endDate.toString());

        if (isCacheExist) {
            throw DuplicateExcelRequestException.withDetail(startDate, endDate);
        }
        excelDownloadCacheRepository.save(ExcelDownloadCache.from(startDate, endDate));
    }

    private List<Dining> fetchDiningData(LocalDate startDate, LocalDate endDate, Boolean isCafeteria) {
        if (isCafeteria) {
            List<String> cafeteriaPlaces = Arrays.asList("A코너", "B코너", "C코너");
            return diningRepository.findByDateBetweenAndPlaceIn(startDate, endDate, cafeteriaPlaces);
        }
        return diningRepository.findByDateBetween(startDate, endDate);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        if (workbook instanceof XSSFWorkbook) {
            XSSFColor customColor = new XSSFColor(new byte[] {(byte)151, (byte)190, (byte)192}, null);
            ((XSSFCellStyle)style).setFillForegroundColor(customColor);
        }

        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private String formatMenu(List<String> menu) {
        return String.join("\n", menu);
    }

    private CellStyle createCommonStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createCornerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);

        if (workbook instanceof XSSFWorkbook) {
            XSSFColor customColor = new XSSFColor(new byte[] {(byte)252, (byte)237, (byte)186}, null);
            ((XSSFCellStyle)style).setFillForegroundColor(customColor);
        }

        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());

        return style;
    }
}
