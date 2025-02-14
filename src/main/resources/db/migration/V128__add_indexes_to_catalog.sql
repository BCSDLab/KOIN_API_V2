-- Catalog 테이블에 인덱스 추가
CREATE INDEX idx_catalog_code_year ON `koin`.`catalog` (code, year);
CREATE INDEX idx_catalog_lecture_name ON `koin`.`catalog` (lecture_name, year);

-- Lecture 테이블에도 인덱스 추가
CREATE INDEX idx_lecture_code_semester ON `koin`.`lectures`(code, semester_date);
