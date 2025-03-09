package in.koreatech.koin.batch.campus.bus.school.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchoolBusTimetable {

    private List<String> nodes;
    private List<Route> to_school;
    private List<Route> from_school;
}
