package in.koreatech.koin.domain.coop.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.coop.dto.CoopLoginRequest;
import in.koreatech.koin.domain.coop.dto.CoopLoginResponse;
import in.koreatech.koin.domain.coop.dto.DiningImageRequest;
import in.koreatech.koin.domain.coop.dto.SoldOutRequest;
import in.koreatech.koin.domain.coop.model.Coop;
import in.koreatech.koin.domain.coop.model.DiningImageUploadEvent;
import in.koreatech.koin.domain.coop.model.DiningSoldOutEvent;
import in.koreatech.koin.domain.coop.repository.CoopRepository;
import in.koreatech.koin.domain.coop.repository.DiningSoldOutCacheRepository;
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
    private final CoopRepository coopRepository;
    private final UserTokenRepository userTokenRepository;
    private final CoopShopService coopShopService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    private final int CELL_NUM = 8;

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

    public ByteArrayInputStream generateCoopExcel(LocalDate startDate, LocalDate endDate, Boolean isCafeteria) {
        List<Dining> dinings;

        if(isCafeteria) {
            List<String> placeFilters = Arrays.asList("A코너", "B코너", "C코너");
            dinings = diningRepository.findByDateBetweenAndPlaceIn(startDate, endDate, placeFilters);
        } else {
            dinings = diningRepository.findByDateBetween(startDate, endDate);
        }


        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("식단 메뉴");

            CellStyle headerStyle = makeHeaderStyle(workbook);
            CellStyle commonStyle = makeCommonStyle(workbook);
            createHeaderCell(sheet, headerStyle);

            return putDiningData(dinings, sheet, commonStyle, workbook);

        } catch (IOException e) {
            throw new RuntimeException("엑셀 파일 생성 중 오류가 발생했습니다.", e);
        }
    }

    private ByteArrayInputStream putDiningData(List<Dining> dinings, Sheet sheet, CellStyle commonStyle,
        Workbook workbook) throws IOException {
        AtomicInteger rowIdx = new AtomicInteger(1);

        dinings.forEach(dining -> {
            Row row = sheet.createRow(rowIdx.getAndIncrement());
            row.createCell(0).setCellValue(dining.getDate().toString());
            row.createCell(1).setCellValue(dining.getType().toString());
            row.createCell(2).setCellValue(dining.getPlace());
            row.createCell(3).setCellValue(dining.getKcal() != null ? dining.getKcal() : 0);

            String formattedMenu = dining.getMenu().toString()
                .replaceAll("^\\[|\\]$", "")
                .replaceAll(", ", "\n");

            Cell menuCell = row.createCell(4);
            menuCell.setCellValue(formattedMenu);

            row.createCell(5).setCellValue(dining.getImageUrl());
            row.createCell(6).setCellValue(dining.getSoldOut());
            row.createCell(7).setCellValue(dining.getIsChanged());

            for (int i = 0; i < 8; i++) {
                row.getCell(i).setCellStyle(commonStyle);
            }
        });

        for (int i = 0; i < CELL_NUM; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, (sheet.getColumnWidth(i) + 1024));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
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

        for (int i = 0; i < CELL_NUM; i++) {
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
