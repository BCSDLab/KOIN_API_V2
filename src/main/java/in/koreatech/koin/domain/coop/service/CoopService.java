package in.koreatech.koin.domain.coop.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
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
import in.koreatech.koin.domain.coop.exception.StartDateAfterEndDateException;
import in.koreatech.koin.domain.coop.model.Coop;
import in.koreatech.koin.domain.coop.model.DiningImageUploadEvent;
import in.koreatech.koin.domain.coop.model.DiningNotifyCache;
import in.koreatech.koin.domain.coop.model.DiningSoldOutEvent;
import in.koreatech.koin.domain.coop.repository.CoopRepository;
import in.koreatech.koin.domain.coop.repository.DiningNotifyCacheRepository;
import in.koreatech.koin.domain.coop.repository.DiningSoldOutCacheRepository;
import in.koreatech.koin.domain.coopshop.model.CoopShopType;
import in.koreatech.koin.domain.coopshop.service.CoopShopService;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.model.DiningType;
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
    private final DiningNotifyCacheRepository diningNotifyCacheRepository;
    private final CoopRepository coopRepository;
    private final UserTokenRepository userTokenRepository;
    private final CoopShopService coopShopService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    List<String> placeFilters = Arrays.asList("A코너", "B코너", "C코너");

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
        dining.setImageUrl(imageRequest.imageUrl());
    }

    public void sendDiningNotify() {
        DiningType diningType = coopShopService.getDiningType();
        LocalDate now = LocalDate.now(clock);
        List<Dining> dinings = diningRepository.findAllByDateAndType(now, diningType);

        if (dinings.isEmpty()) {
            return;
        }

        boolean allImageExist = diningRepository.allExistsByDateAndTypeAndPlacesAndImageUrlIsNotNull(
            now, diningType, placeFilters
        );
        boolean isOpened = coopShopService.getIsOpened(LocalDateTime.now(clock), CoopShopType.CAFETERIA, diningType, true);
        String diningNotifyId = now.toString() + diningType;

        if (isOpened && allImageExist) {
            if (alreadyNotify(diningNotifyId))
                return;

            if (!diningNotifyCacheRepository.existsById(diningNotifyId)) {
                sendNotify(diningNotifyId, dinings);
            }

            if (LocalTime.now().isAfter(diningType.getStartTime().minusMinutes(10))
                && !diningNotifyCacheRepository.existsById(diningNotifyId)
            ) {
                sendNotify(diningNotifyId, dinings);
            }
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
    }

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
        checkDate(startDate, endDate);

        List<Dining> dinings;

        if (isCafeteria) {
            dinings = diningRepository.findByDateBetweenAndPlaceIn(startDate, endDate, placeFilters);
        } else {
            dinings = diningRepository.findByDateBetween(startDate, endDate);
        }

        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            SXSSFSheet sheet = workbook.createSheet("식단 메뉴");
            sheet.setRandomAccessWindowSize(100);

            CellStyle headerStyle = makeHeaderStyle(workbook);
            CellStyle commonStyle = makeCommonStyle(workbook);
            createHeaderCell(sheet, headerStyle);

            ByteArrayInputStream result = putDiningData(dinings, sheet, commonStyle, workbook);

            return result;

        } catch (IOException e) {
            throw new RuntimeException("엑셀 파일 생성 중 오류가 발생했습니다.", e);
        }
    }

    private static void checkDate(LocalDate startDate, LocalDate endDate) {
        LocalDate limitDate = LocalDate.of(2022, 11, 29);
        if (startDate.isBefore(limitDate) || endDate.isBefore(limitDate)) {
            throw new DiningLimitDateException("2022/11/29 식단부터 다운받을 수 있어요.");
        }

        LocalDate now = LocalDate.now();
        if (startDate.isAfter(now) || endDate.isAfter(now)) {
            throw new DiningNowDateException("오늘 날짜 이후 기간은 설정할 수 없어요.");
        }

        if (startDate.isAfter(endDate)) {
            throw new StartDateAfterEndDateException("시작일은 종료일 이전으로 설정해주세요.");
        }
    }

    private ByteArrayInputStream putDiningData(List<Dining> dinings, SXSSFSheet sheet, CellStyle commonStyle,
        SXSSFWorkbook workbook) throws IOException {
        AtomicInteger rowIdx = new AtomicInteger(1);

        dinings.forEach(dining -> {
            SXSSFRow row = sheet.createRow(rowIdx.getAndIncrement());
            row.createCell(0).setCellValue(dining.getDate().toString());
            row.createCell(1).setCellValue(dining.getType().getDiningName());
            row.createCell(2).setCellValue(dining.getPlace());
            row.createCell(3).setCellValue(dining.getKcal() != null ? dining.getKcal() : 0);

            String formattedMenu = dining.getMenu().toString()
                .replaceAll("^\\[|\\]$", "")
                .replaceAll(", ", "\n");

            SXSSFCell menuCell = row.createCell(4);
            menuCell.setCellValue(formattedMenu);

            row.createCell(5).setCellValue(dining.getImageUrl());
            row.createCell(6).setCellValue(
                Optional.ofNullable(dining.getSoldOut()).map(Object::toString).orElse("")
            );
            row.createCell(7).setCellValue(Optional.ofNullable(dining.getIsChanged()).map(Object::toString).orElse(""));

            for (int i = 0; i < EXCEL_COLUMN_COUNT; i++) {
                row.getCell(i).setCellStyle(commonStyle);
            }
        });

        for (int i = 0; i < EXCEL_COLUMN_COUNT; i++) {
            sheet.setColumnWidth(i, 6000);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        workbook.dispose();
        return new ByteArrayInputStream(out.toByteArray());
    }

    private void createHeaderCell(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("날짜");
        headerRow.createCell(1).setCellValue("타입");
        headerRow.createCell(2).setCellValue("코너");
        headerRow.createCell(3).setCellValue("칼로리");
        headerRow.createCell(4).setCellValue("메뉴");
        headerRow.createCell(5).setCellValue("이미지");
        headerRow.createCell(6).setCellValue("품절 여부");
        headerRow.createCell(7).setCellValue("변경 여부");

        for (int i = 0; i < EXCEL_COLUMN_COUNT; i++) {
            Cell cell = headerRow.getCell(i);
            cell.setCellStyle(headerStyle);
        }
    }

    private static CellStyle makeCommonStyle(Workbook workbook) {
        CellStyle commonStyle = workbook.createCellStyle();
        commonStyle.setAlignment(HorizontalAlignment.CENTER);
        commonStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        commonStyle.setWrapText(true);
        return commonStyle;
    }

    private static CellStyle makeHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        return headerStyle;
    }
}
