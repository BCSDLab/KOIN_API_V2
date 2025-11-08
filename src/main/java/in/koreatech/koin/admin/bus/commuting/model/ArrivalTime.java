package in.koreatech.koin.admin.bus.commuting.model;

public record ArrivalTime(
    String time
) {
    public static ArrivalTime of(String time) {
        return new ArrivalTime(time);
    }
}
