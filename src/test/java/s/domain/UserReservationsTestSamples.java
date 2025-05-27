package s.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class UserReservationsTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static UserReservations getUserReservationsSample1() {
        return new UserReservations().id(1L).totalSize(1).usedSize(1).createdBy("createdBy1");
    }

    public static UserReservations getUserReservationsSample2() {
        return new UserReservations().id(2L).totalSize(2).usedSize(2).createdBy("createdBy2");
    }

    public static UserReservations getUserReservationsRandomSampleGenerator() {
        return new UserReservations()
            .id(longCount.incrementAndGet())
            .totalSize(intCount.incrementAndGet())
            .usedSize(intCount.incrementAndGet())
            .createdBy(UUID.randomUUID().toString());
    }
}
