package in.koreatech.koin.domain.bus.service.shuttle.internal;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import in.koreatech.koin.domain.bus.enums.BusDirection;
import in.koreatech.koin.domain.bus.enums.BusStation;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;

/**
 * Searches one shuttle route without changing the order or raw values held by the route.
 */
public final class ShuttleRoutePathSelector {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
        .withResolverStyle(ResolverStyle.STRICT);
    private static final Set<String> DESTINATION_MARKERS = Set.of("도착", "하차", "승하차", "정차", "종점");

    private ShuttleRoutePathSelector() {
    }

    public record Occurrence(int departureIndex, int arrivalIndex, LocalTime departureTime) {
    }

    public static List<Occurrence> select(
        List<String> nodeNames,
        List<String> arrivalTimes,
        ShuttleRouteType routeType,
        String routeDirection,
        BusStation depart,
        BusStation arrival
    ) {
        if (depart == arrival) {
            return List.of();
        }
        return select(nodeNames, arrivalTimes, routeType, routeDirection,
            aliasesFor(depart), aliasesFor(arrival));
    }

    public static List<Occurrence> select(
        List<String> nodeNames,
        List<String> arrivalTimes,
        ShuttleRouteType routeType,
        String routeDirection,
        Set<String> departureAliases,
        Set<String> arrivalAliases
    ) {
        if (nodeNames == null || arrivalTimes == null || nodeNames.isEmpty()
            || nodeNames.size() != arrivalTimes.size()
            || departureAliases == null || arrivalAliases == null) {
            return List.of();
        }

        List<Integer> orderedIndexes = orderedIndexes(nodeNames, arrivalTimes, routeType, routeDirection);
        List<Occurrence> occurrences = new ArrayList<>();
        for (int position = 0; position < orderedIndexes.size(); position++) {
            int departureIndex = orderedIndexes.get(position);
            if (isTerminal(arrivalTimes.get(departureIndex))) {
                break;
            }
            if (!matches(departureAliases, nodeNames.get(departureIndex))) {
                continue;
            }

            LocalTime departureTime = parseTime(arrivalTimes.get(departureIndex));
            if (departureTime == null) {
                continue;
            }

            for (int nextPosition = position + 1; nextPosition < orderedIndexes.size(); nextPosition++) {
                int arrivalIndex = orderedIndexes.get(nextPosition);
                String rawArrivalTime = arrivalTimes.get(arrivalIndex);
                if (matches(arrivalAliases, nodeNames.get(arrivalIndex))
                    && isValidDestinationTime(rawArrivalTime, departureTime)) {
                    occurrences.add(new Occurrence(departureIndex, arrivalIndex, departureTime));
                    break;
                }

                if (isTerminal(rawArrivalTime)) {
                    break;
                }
            }
        }
        return occurrences;
    }

    public static Set<String> aliasesFor(BusStation station) {
        Set<String> aliases = new HashSet<>(station.getDisplayNames());
        if (station == BusStation.STATION) {
            aliases.add("천안역A");
            aliases.add("천안역B");
        }
        return Set.copyOf(aliases);
    }

    public static boolean shouldReverse(
        List<String> nodeNames,
        List<String> arrivalTimes,
        ShuttleRouteType routeType,
        String routeDirection
    ) {
        if (routeType != ShuttleRouteType.WEEKDAYS
            || nodeNames == null || arrivalTimes == null
            || nodeNames.isEmpty() || nodeNames.size() != arrivalTimes.size()) {
            return false;
        }

        boolean south = BusDirection.SOUTH.getName().equals(routeDirection);
        boolean north = BusDirection.NORTH.getName().equals(routeDirection);
        if (!south && !north) {
            return false;
        }

        boolean campusTimedAtStart = isTimedCampus(nodeNames, arrivalTimes, 0);
        boolean campusTimedAtEnd = isTimedCampus(nodeNames, arrivalTimes, nodeNames.size() - 1);
        if (south) {
            return campusTimedAtEnd && !campusTimedAtStart;
        }
        return campusTimedAtStart && !campusTimedAtEnd;
    }

    private static List<Integer> orderedIndexes(
        List<String> nodeNames,
        List<String> arrivalTimes,
        ShuttleRouteType routeType,
        String routeDirection
    ) {
        List<Integer> indexes = new ArrayList<>();
        for (int index = 0; index < nodeNames.size(); index++) {
            indexes.add(index);
        }
        if (shouldReverse(nodeNames, arrivalTimes, routeType, routeDirection)) {
            java.util.Collections.reverse(indexes);
        }
        return indexes;
    }

    private static boolean isTimedCampus(List<String> nodeNames, List<String> arrivalTimes, int index) {
        return matches(aliasesFor(BusStation.KOREATECH), nodeNames.get(index))
            && parseTime(arrivalTimes.get(index)) != null;
    }

    private static boolean matches(Set<String> aliases, String nodeName) {
        return nodeName != null && aliases.contains(nodeName.trim());
    }

    private static boolean isValidDestinationTime(String rawTime, LocalTime departureTime) {
        if (isDestinationMarker(rawTime)) {
            return true;
        }
        LocalTime arrivalTime = parseTime(rawTime);
        return arrivalTime != null && arrivalTime.isAfter(departureTime);
    }

    private static boolean isTerminal(String rawTime) {
        return "종점".equals(normalize(rawTime));
    }

    private static boolean isDestinationMarker(String rawTime) {
        String normalized = normalize(rawTime);
        return normalized != null && DESTINATION_MARKERS.contains(normalized);
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private static LocalTime parseTime(String value) {
        String normalized = normalize(value);
        if (normalized == null || normalized.isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(normalized, TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}
