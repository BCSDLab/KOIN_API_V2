package in.koreatech.koin.domain.coop.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

import org.apache.commons.lang3.RandomStringUtils;
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
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import in.koreatech.koin._common.auth.JwtProvider;
import in.koreatech.koin._common.event.DiningImageUploadEvent;
import in.koreatech.koin._common.event.DiningSoldOutEvent;
import in.koreatech.koin._common.exception.custom.KoinIllegalStateException;
import in.koreatech.koin.domain.coop.dto.CoopLoginRequest;
import in.koreatech.koin.domain.coop.dto.CoopLoginResponse;
import in.koreatech.koin.domain.coop.dto.DiningImageRequest;
import in.koreatech.koin.domain.coop.dto.SoldOutRequest;
import in.koreatech.koin.domain.coop.exception.DiningLimitDateException;
import in.koreatech.koin.domain.coop.exception.DiningNowDateException;
import in.koreatech.koin.domain.coop.exception.DuplicateExcelRequestException;
import in.koreatech.koin.domain.coop.exception.StartDateAfterEndDateException;
import in.koreatech.koin.domain.coop.model.Coop;
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
import in.koreatech.koin.domain.coop.dto.CoopResponse;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserTokenRedisRepository;
import in.koreatech.koin.infrastructure.s3.client.S3Client;
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
    private final UserRepository userRepository;
    private final UserTokenRedisRepository userTokenRedisRepository;
    private final CoopShopService coopShopService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final S3Client s3Client;
    private final List<String> placeFilters = Arrays.asList("A코너", "B코너", "C코너");
    private final List<String> cafeteriaPlaceFilters = Arrays.asList("A코너", "B코너", "C코너");
    private final List<String> allPlaceFilters = Arrays.asList("A코너", "B코너", "C코너", "능수관", "2캠퍼스");

    public static final LocalDate LIMIT_DATE = LocalDate.of(2022, 11, 29);
    private static final int mealColumIndex = 0;
    private static final int cornerColumnIndex = 1;
    private static final int mealAndColumnWidth = 4000;

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

        user.requireSameLoginPw(passwordEncoder, request.password());

        String accessToken = jwtProvider.createToken(user);
        String refreshToken = String.format("%s-%d", UUID.randomUUID(), user.getId());
        UserToken savedToken = userTokenRedisRepository.save(UserToken.create(user.getId(), refreshToken));
        user.updateLastLoggedTime(LocalDateTime.now());

        return CoopLoginResponse.of(accessToken, savedToken.getRefreshToken());
    }

    public CoopResponse getCoop(Integer userId) {
        User user = userRepository.getById(userId);
        return CoopResponse.from(user);
    }

    public ByteArrayInputStream generateDiningExcel(LocalDate startDate, LocalDate endDate, Boolean isCafeteria) {
        checkDuplicateExcelRequest(startDate, endDate);
        validateDates(startDate, endDate);
        List<Dining> dinings = fetchDiningData(startDate, endDate, isCafeteria);

        Map<LocalDate, List<Dining>> dateDinings = dinings.stream()
            .collect(Collectors.groupingBy(
                Dining::getDate,
                TreeMap::new,
                Collectors.toList()
            ));

        ByteArrayInputStream excelDesign = createDiningExcelWithDetailedFormat(dateDinings, isCafeteria);

        return excelDesign;
    }

    private ByteArrayInputStream createDiningExcelWithDetailedFormat(
        Map<LocalDate, List<Dining>> dateDinings, Boolean isCafeteria
    ) {
        Workbook workbook = new XSSFWorkbook();
        Map<String, CellStyle> cellStyles = initializeWorkbookAndStyles(workbook);
        Sheet sheet = workbook.createSheet("식단");

        Map<Integer, Row> rowCache = initializeRowCache(sheet, 100);
        List<String> meals = List.of("조식", "중식", "석식");
        List<String> corners = isCafeteria ? cafeteriaPlaceFilters : allPlaceFilters;

        addMealAndCornerData(sheet, rowCache, meals, corners, cellStyles.get("cornerStyle"));
        addDateAndDiningData(sheet, rowCache, dateDinings, cellStyles, isCafeteria);

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

    private Map<String, CellStyle> initializeWorkbookAndStyles(Workbook workbook) {
        Map<String, CellStyle> styles = new HashMap<>();
        styles.put("headerStyle", getHeaderStyle(workbook));
        styles.put("commonStyle", getCommonStyle(workbook));
        styles.put("cornerStyle", getCornerStyle(workbook));

        return styles;
    }

    private Map<Integer, Row> initializeRowCache(Sheet sheet, int numRows) {
        Map<Integer, Row> rowCache = new HashMap<>();
        for (int i = 0; i < numRows; i++) {
            rowCache.put(i, sheet.createRow(i));
        }

        return rowCache;
    }

    private void addMealAndCornerData(
        Sheet sheet, Map<Integer, Row> rowCache, List<String> meals, List<String> corners, CellStyle cornerStyle
    ) {
        int rowIndex = 1;

        for (String meal : meals) {
            int mealStartRow = rowIndex;

            for (String corner : corners) {
                int cornerStartRow = rowIndex;
                rowIndex += cafeteriaPlaceFilters.contains(corner) ? 4 : 2;

                sheet.addMergedRegion(
                    new CellRangeAddress(cornerStartRow, rowIndex - 1, cornerColumnIndex, cornerColumnIndex));
                fillMergedCellsWithStyle(rowCache, cornerStartRow, rowIndex - 1, cornerColumnIndex, corner,
                    cornerStyle);
            }

            sheet.addMergedRegion(new CellRangeAddress(mealStartRow, rowIndex - 1, mealColumIndex, mealColumIndex));
            setCellValueWithStyle(rowCache.get(mealStartRow), mealColumIndex, meal, cornerStyle);
        }

        sheet.setColumnWidth(mealColumIndex, mealAndColumnWidth);
        sheet.setColumnWidth(cornerColumnIndex, mealAndColumnWidth);
    }

    private void fillMergedCellsWithStyle(
        Map<Integer, Row> rowCache, int startRow, int endRow, int col, String value, CellStyle style
    ) {
        for (int row = startRow; row <= endRow; row++) {
            Row currentRow = rowCache.get(row);
            setCellValueWithStyle(currentRow, col, value, style);
        }
    }

    private void setCellValueWithStyle(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void addDateAndDiningData(
        Sheet sheet, Map<Integer, Row> rowCache, Map<LocalDate, List<Dining>> dateDinings,
        Map<String, CellStyle> styles, Boolean isCafeteria
    ) {
        int colIndex = 2;

        for (Map.Entry<LocalDate, List<Dining>> entry : dateDinings.entrySet()) {
            LocalDate date = entry.getKey();
            List<Dining> dinings = entry.getValue();

            setCellValueWithStyle(rowCache.get(0), colIndex, date.toString(), styles.get("headerStyle"));

            for (Dining dining : dinings) {
                ExcelDiningPosition position = ExcelDiningPosition.from(dining.getType(), dining.getPlace());
                int startPosition = isCafeteria
                    ? position.getStartPositionOnlyCafeteria()
                    : position.getStartPositionAllPlace();

                if (cafeteriaPlaceFilters.contains(dining.getPlace())) {
                    drawExcelCafeteria(dining, rowCache, startPosition, colIndex, styles.get("commonStyle"));
                } else {
                    drawExcelNoneCafeteria(dining, rowCache, startPosition, colIndex, styles.get("commonStyle"));
                }
            }

            sheet.setColumnWidth(colIndex, 6000);
            colIndex++;
        }
    }

    private void drawExcelNoneCafeteria(Dining dining, Map<Integer, Row> rowCache, int startPosition, int colIndex,
        CellStyle cellStyle
    ) {
        Row menuRow = rowCache.get(startPosition);
        Cell menuCell = menuRow.createCell(colIndex);
        menuCell.setCellValue(formatMenu(dining.getMenu()));
        menuCell.setCellStyle(cellStyle);

        Row kcalRow = rowCache.get(startPosition + 1);
        Cell kcalCell = kcalRow.createCell(colIndex);
        kcalCell.setCellValue(dining.getKcal() == null ? "0 kcal" : dining.getKcal() + " kcal");
        kcalCell.setCellStyle(cellStyle);
    }

    private void drawExcelCafeteria(Dining dining, Map<Integer, Row> rowCache, int startPosition, int colIndex,
        CellStyle cellStyle) {
        Row menuRow = rowCache.get(startPosition);
        Cell menuCell = menuRow.createCell(colIndex);
        menuCell.setCellValue(formatMenu(dining.getMenu()));
        menuCell.setCellStyle(cellStyle);

        Row imageRow = rowCache.get(startPosition + 1);
        Cell imageCell = imageRow.createCell(colIndex);
        imageCell.setCellValue(dining.getImageUrl());
        imageCell.setCellStyle(cellStyle);

        Row kcalRow = rowCache.get(startPosition + 2);
        Cell kcalCell = kcalRow.createCell(colIndex);
        kcalCell.setCellValue(dining.getKcal() == null ? "0 kcal" : dining.getKcal() + " kcal");
        kcalCell.setCellStyle(cellStyle);

        Row soldOutRow = rowCache.get(startPosition + 3);
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
            return diningRepository.findByDateBetweenAndPlaceIn(startDate, endDate, cafeteriaPlaceFilters);
        }

        return diningRepository.findByDateBetween(startDate, endDate);
    }

    private String formatMenu(List<String> menu) {
        return String.join("\n", menu);
    }

    private CellStyle createCellStyle(Workbook workbook, byte[] backgroundColor) {
        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);

        if (backgroundColor != null && workbook instanceof XSSFWorkbook) {
            XSSFColor customColor = new XSSFColor(backgroundColor, null);
            ((XSSFCellStyle)style).setFillForegroundColor(customColor);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setWrapText(true);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        short borderColor = IndexedColors.GREY_25_PERCENT.getIndex();
        style.setTopBorderColor(borderColor);
        style.setBottomBorderColor(borderColor);
        style.setLeftBorderColor(borderColor);
        style.setRightBorderColor(borderColor);

        return style;
    }

    private CellStyle getCommonStyle(Workbook workbook) {
        return createCellStyle(workbook, null);
    }

    private CellStyle getHeaderStyle(Workbook workbook) {
        return createCellStyle(workbook, new byte[] {(byte)151, (byte)190, (byte)192});
    }

    private CellStyle getCornerStyle(Workbook workbook) {
        return createCellStyle(workbook, new byte[] {(byte)252, (byte)237, (byte)186});
    }

    /**
     * 영양사 페이지 > 식단 이미지 압축파일 다운로드 기능
     * 동작 과정
     * 1. 요청한 기간 내에 이미지가 존재하는 식단 데이터를 조회한다.
     * 2. 프로젝트 최상위 경로에 image-download/dining_images 폴더를 생성한다.
     * 4. 식단 이미지를 S3에서 다운로드하여 dining_images 폴더에 저장한다.
     * 5. dining_images 폴더를 dining_images.zip 파일로 압축한다.
     * 6. dining_images 폴더를 제거하고 dining_images.zip 파일을 반환한다.
     * 7. 압축파일 반환 전, 새로운 스레드를 생성하여 dining_images.zip 파일을 삭제한다.
     *
     * 문제 발생 가능한 부분
     * (1) 4번 과정에서 서버 자원을 과하게 많이 사용할 수 있다.(물론 요청 이후에는 삭제되지만 요청 간 자원이 많이 사용된다)
     * (2) 6, 7번 과정에서 파일 제거가 실패할 경우, 서버에 임시 파일이 남아있을 수 있다.
     *
     * 개선 가능 부분
     * - (4)를 개선하기 위해 이미지 다운로드 시, 서버에 직접 다운받지 않고 S3에서 즉시 사용자에게 전송한다.
     *   - 참고 자료) https://gksdudrb922.tistory.com/234
     * - (2)를 개선하기 위해 파일 제거 실패 시, 재시도를 하거나 예외를 던지거나 스케줄링을 돌린다.
     *
     * @param startDate 시작일
     * @param endDate 종료일
     * @param isCafeteria 학식당 이미지만 다운로드할 것인가
     * @return 식단 이미지 압축파일
     */
    public File generateDiningImageCompress(LocalDate startDate, LocalDate endDate, Boolean isCafeteria) {
        validateDates(startDate, endDate);
        List<Dining> dining = fetchDiningDataForImageCompress(startDate, endDate, isCafeteria);
        return generateZipFileOf(dining);
    }

    private List<Dining> fetchDiningDataForImageCompress(LocalDate startDate, LocalDate endDate, Boolean isCafeteria) {
        if (isCafeteria) {
            return diningRepository.findByDateBetweenAndImageUrlIsNotNullAndPlaceIn(startDate, endDate, placeFilters);
        }
        return diningRepository.findByDateBetweenAndImageUrlIsNotNull(startDate, endDate);
    }

    private File generateZipFileOf(List<Dining> dinings) {
        String bucketName = s3Client.getBucketName();
        File parentDirectory = new File("image-download", RandomStringUtils.randomAlphanumeric(6));
        File localImageDirectory = new File(parentDirectory, "dining_images");
        File zipFile = new File(parentDirectory, "dining_images.zip");
        preprocessPath(localImageDirectory, zipFile);

        for (Dining dining : dinings) {
            if (dining.getImageUrl().isEmpty()
                || !dining.getImageUrl().startsWith(s3Client.getDomainUrlPrefix())) {
                continue;
            }
            String s3Key = extractS3KeyFrom(dining.getImageUrl());
            File localFile = new File(localImageDirectory, convertFileName(dining, s3Key));
            s3Client.downloadS3Object(bucketName, s3Key, localFile);
        }
        compress(zipFile, localImageDirectory);
        remove(localImageDirectory);
        return zipFile;
    }

    private String convertFileName(Dining dining, String s3Key) {
        String extension = s3Key.substring(s3Key.lastIndexOf("."));
        LocalDate date = dining.getDate();
        // ex) 2024-12-17-점심-B코너.png
        return date.getYear() + "-" + date.getMonthValue() + "-" + date.getDayOfMonth() + "-"
               + dining.getType().getDiningName() + "-" + dining.getPlace() + extension;
    }

    private String extractS3KeyFrom(String imageUrl) {
        // URL format: https://<bucket-name>/<key(경로+파일명)>
        String cdnPath = s3Client.getDomainUrlPrefix();
        return imageUrl.substring(imageUrl.indexOf(cdnPath) + cdnPath.length());
    }

    private void preprocessPath(File localImageDirectory, File zipFilePath) {
        if (!localImageDirectory.exists()) {
            localImageDirectory.mkdirs();
        }
        if (zipFilePath.exists()) {
            zipFilePath.delete();
        }
    }

    public void removeDiningImageCompress(File zipFilePath) {
        new Thread(() -> remove(zipFilePath.getParentFile())).start();
    }

    private void compress(File path, File localImageDirectory) {
        try {
            new ZipFile(path).addFolder(localImageDirectory);
        } catch (ZipException e) {
            throw new KoinIllegalStateException("파일 압축 중 문제가 발생했습니다. " + e.getMessage());
        }
    }

    private void remove(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        directory.delete();
    }
}
