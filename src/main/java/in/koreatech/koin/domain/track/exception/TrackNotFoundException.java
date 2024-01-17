package in.koreatech.koin.domain.track.exception;

public class TrackNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 트랙입니다.";

    public TrackNotFoundException(String message) {
        super(message);
    }

    public static TrackNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new TrackNotFoundException(message);
    }
}
