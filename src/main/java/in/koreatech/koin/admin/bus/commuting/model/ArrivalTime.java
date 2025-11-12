package in.koreatech.koin.admin.bus.commuting.model;

public record ArrivalTime(
    String time
) {
    public static ArrivalTime from(String time) {
        return new ArrivalTime(time);
    }
}
