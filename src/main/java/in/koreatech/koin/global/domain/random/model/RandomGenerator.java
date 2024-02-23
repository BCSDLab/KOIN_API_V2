package in.koreatech.koin.global.domain.random.model;

import java.util.concurrent.ThreadLocalRandom;

public class RandomGenerator {

    public static int createNumber(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }
}
