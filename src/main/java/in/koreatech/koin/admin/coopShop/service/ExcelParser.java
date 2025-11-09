package in.koreatech.koin.admin.coopShop.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.coopShop.model.CoopShopRow;

public interface ExcelParser {

    List<CoopShopRow> parse(MultipartFile multipartFile);
}
