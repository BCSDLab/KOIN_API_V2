package in.koreatech.koin.domain.graduation.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.repository.CourseTypeRepository;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.exception.ExcelFileCheckException;
import in.koreatech.koin.domain.timetableV2.exception.ExcelFileNotFoundException;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV2.repository.LectureRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.SemesterRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GraduationService {

    private final CourseTypeRepository courseTypeRepository;
    private final LectureRepositoryV2 lectureRepositoryV2;
    private final TimetableLectureRepositoryV2 timetableLectureRepositoryV2;
    private final UserRepository userRepository;
    private final SemesterRepositoryV2 semesterRepositoryV2;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;

    @Transactional
    public void readStudentGradeExcelFile(MultipartFile file, Integer userId) throws IOException {
        checkFiletype(file);

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new HSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            TimetableFrame graduationFrame = null;
            String currentSemester = "default";

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;

                String excelYear = getCellValueAsString(row.getCell(1));
                String excelSemester = getCellValueAsString(row.getCell(2));
                String excelCode = getCellValueAsString(row.getCell(4));
                String excelClassTitle = getCellValueAsString(row.getCell(5));
                String excelLectureClass = getCellValueAsString(row.getCell(6));
                String excelProfessor = getCellValueAsString(row.getCell(7));
                String excelCourseType = getCellValueAsString(row.getCell(8));
                String excelGrades = getCellValueAsString(row.getCell(9));
                String excelRetakeStatus = getCellValueAsString(row.getCell(11));

                if (excelClassTitle.equals("소 계") || excelRetakeStatus.equals("Y"))
                    continue; // 재수강 한 경우에는 엑셀에 값이 중복되서 들어 갈 수 있으므로
                if (excelClassTitle.equals("합 계"))
                    break;

                String semester = getKoinSemester(excelSemester, excelYear);
                CourseType courseType = courseTypeRepository.findByName(excelCourseType).orElse(null);
                Lecture lecture = lectureRepositoryV2.findBySemesterAndCodeAndLectureClass(semester,
                    excelCode, excelLectureClass).orElse(null);

                if (!currentSemester.equals(semester)) {
                    currentSemester = semester;
                    graduationFrame = createFrameAboutExcel(userId, currentSemester);
                }

                if (lecture != null) {
                    TimetableLecture timetableLecture = TimetableLecture.builder()
                        .classTitle(excelClassTitle)
                        .classTime(null)
                        .classPlace(null)
                        .professor(excelProfessor)
                        .grades(excelGrades)
                        .memo(null)
                        .isDeleted(false)
                        .lecture(lecture)
                        .timetableFrame(graduationFrame)
                        .courseType(courseType)
                        .build();
                    timetableLectureRepositoryV2.save(timetableLecture);
                } else {
                    TimetableLecture timetableLecture = TimetableLecture.builder()
                        .classTitle(excelClassTitle)
                        .classTime(null)
                        .classPlace(null)
                        .professor(excelProfessor)
                        .grades(excelGrades)
                        .memo(null)
                        .isDeleted(false)
                        .lecture(null)
                        .timetableFrame(graduationFrame)
                        .courseType(courseType)
                        .build();
                    timetableLectureRepositoryV2.save(timetableLecture);
                }
            }
        }
    }

    private TimetableFrame createFrameAboutExcel(Integer userId, String semester) {
        User user = userRepository.getById(userId);
        Semester saveSemester = semesterRepositoryV2.getBySemester(semester);

        List<TimetableFrame> timetableFrameList = timetableFrameRepositoryV2.findAllByUserIdAndSemesterId
            (userId, saveSemester.getId());
        for (TimetableFrame timetableFrame : timetableFrameList) {
            if (timetableFrame.isMain()) {
                timetableFrame.cancelMain();
            }
        }
        TimetableFrame graduationFrame = TimetableFrame.builder()
            .user(user)
            .semester(saveSemester)
            .name("졸업학점 계산용 테이블")
            .isDeleted(false)
            .isMain(true)
            .build();
        timetableFrameRepositoryV2.save(graduationFrame);

        return graduationFrame;
    }

    private void checkFiletype(MultipartFile file) {
        if (file == null) {
            throw new ExcelFileNotFoundException("파일이 있는지 확인 해주세요.");
        }

        String fileName = file.getOriginalFilename();
        int findDot = fileName.lastIndexOf(".");
        if (findDot == -1) {
            throw new ExcelFileNotFoundException("파일의 형식이 맞는지 확인 해주세요.");
        }

        String extension = fileName.substring(findDot + 1);
        if (!extension.equals("xls") && !extension.equals("xlsx")) {
            throw new ExcelFileCheckException("엑셀 파일인지 확인 해주세요.");
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int)cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private String getKoinSemester(String semester, String year) {
        if (semester.equals("1") || semester.equals("2")) {
            return year + semester;
        } else if (semester.equals("동계")) {
            return year + "-" + "겨울";
        } else
            return year + "-" + "여름";
    }
}
