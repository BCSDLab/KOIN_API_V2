package in.koreatech.koin.global.domain.random.model;

import static lombok.AccessLevel.PRIVATE;

import java.util.concurrent.ThreadLocalRandom;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class RandomGenerator {

    public static int createNumber(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }
}
