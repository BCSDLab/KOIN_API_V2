package in.koreatech.koin.admin.coopShop.parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.coopShop.model.CoopShopRow;
import in.koreatech.koin.admin.coopShop.service.ExcelParser;

@Component
public class POIExcelParser implements ExcelParser {

    @Override
    public List<CoopShopRow> parse(MultipartFile inputStream) {
        Workbook workbook = getWorkBook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        List<CoopShopRow> coopShops = new ArrayList<>();

        String coopName = "";
        String phone = "";
        String location = "";
        String remark = "";
        String type = "";
        String dayOfWeek = "";
        String openTime = "";
        String closeTime = "";

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            coopName = getCellValue(row.getCell(0), coopName);
            phone = getCellValue(row.getCell(1), phone);
            location = getCellValue(row.getCell(2), location);
            remark = getCellValue(row.getCell(3), remark);
            type = getCellValue(row.getCell(4), type);
            dayOfWeek = getCellValue(row.getCell(5), dayOfWeek);
            openTime = getCellValue(row.getCell(6), openTime);
            closeTime = getCellValue(row.getCell(7), closeTime);

            coopShops.add(
                new CoopShopRow(coopName, phone, location, remark, type, dayOfWeek, openTime, closeTime)
            );
        }

        return coopShops;
    }

    private Workbook getWorkBook(MultipartFile inputStream) {
        try (Workbook workbook = WorkbookFactory.create(inputStream.getInputStream())) {
            return workbook;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getCellValue(Cell cell, String cellValue) {
        if (cell.getStringCellValue().isBlank()) {
            return cellValue;
        }
        return cell.getStringCellValue();
    }
}
