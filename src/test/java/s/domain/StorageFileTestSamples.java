package s.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StorageFileTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static StorageFile getStorageFileSample1() {
        return new StorageFile().id(1L).name("name1").size(1).mimeType("mimeType1").path("path1").createdBy("createdBy1");
    }

    public static StorageFile getStorageFileSample2() {
        return new StorageFile().id(2L).name("name2").size(2).mimeType("mimeType2").path("path2").createdBy("createdBy2");
    }

    public static StorageFile getStorageFileRandomSampleGenerator() {
        return new StorageFile()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .size(intCount.incrementAndGet())
            .mimeType(UUID.randomUUID().toString())
            .path(UUID.randomUUID().toString())
            .createdBy(UUID.randomUUID().toString());
    }
}
