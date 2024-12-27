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

import in.koreatech.koin.domain.graduation.dto.ExcelStudentData;
import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.repository.CourseTypeRepository;
import in.koreatech.koin.domain.graduation.exception.ExcelFileCheckException;
import in.koreatech.koin.domain.graduation.exception.ExcelFileNotFoundException;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
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

    private static final String MIDDLE_TOTAL = "소 계";
    private static final String TOTAL = "합 계";
    private static final String RETAKE = "Y";
    private static final String UNSATISFACTORY = "U";

    private final CourseTypeRepository courseTypeRepository;
    private final UserRepository userRepository;
    private final SemesterRepositoryV2 semesterRepositoryV2;
    private final LectureRepositoryV2 lectureRepositoryV2;
    private final TimetableLectureRepositoryV2 timetableLectureRepositoryV2;
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
                ExcelStudentData data = extractStudentData(row);
                if (row.getRowNum() == 0 || skipRow(data))
                    continue;

                if (data.excelClassTitle().equals(TOTAL))
                    break;

                String semester = getKoinSemester(data.excelSemester(), data.excelYear());
                CourseType courseType = courseTypeRepository.findByName(data.excelCourseType()).orElse(null);
                Lecture lecture = lectureRepositoryV2.findBySemesterAndCodeAndLectureClass(semester,
                    data.excelCode(), data.excelLectureClass()).orElse(null);

                if (!currentSemester.equals(semester)) {
                    currentSemester = semester;
                    graduationFrame = createFrameAboutExcel(userId, currentSemester);
                }

                TimetableLecture timetableLecture = TimetableLecture.builder()
                    .classTitle(data.excelClassTitle())
                    .classTime(lecture != null ? lecture.getClassTime() : null)
                    .classPlace(null)
                    .professor(data.excelProfessor())
                    .grades(data.excelCredit())
                    .memo(null)
                    .isDeleted(false)
                    .lecture(lecture)
                    .timetableFrame(graduationFrame)
                    .courseType(courseType)
                    .build();

                timetableLectureRepositoryV2.save(timetableLecture);
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

    private ExcelStudentData extractStudentData(Row row) {
        return new ExcelStudentData(
            getCellValueAsString(row.getCell(1)),
            getCellValueAsString(row.getCell(2)),
            getCellValueAsString(row.getCell(4)),
            getCellValueAsString(row.getCell(5)),
            getCellValueAsString(row.getCell(6)),
            getCellValueAsString(row.getCell(7)),
            getCellValueAsString(row.getCell(8)),
            getCellValueAsString(row.getCell(9)),
            getCellValueAsString(row.getCell(10)),
            getCellValueAsString(row.getCell(11))
        );
    }

    private boolean skipRow(ExcelStudentData excelStudentData) {
        return excelStudentData.excelClassTitle().equals(MIDDLE_TOTAL) ||
               excelStudentData.excelRetakeStatus().equals(RETAKE) ||
               excelStudentData.excelGrade().equals(UNSATISFACTORY);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int)cell.getNumericCellValue());
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
