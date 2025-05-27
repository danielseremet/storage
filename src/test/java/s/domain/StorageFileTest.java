package s.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static s.domain.StorageFileTestSamples.*;

import org.junit.jupiter.api.Test;
import s.web.rest.TestUtil;

class StorageFileTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(StorageFile.class);
        StorageFile storageFile1 = getStorageFileSample1();
        StorageFile storageFile2 = new StorageFile();
        assertThat(storageFile1).isNotEqualTo(storageFile2);

        storageFile2.setId(storageFile1.getId());
        assertThat(storageFile1).isEqualTo(storageFile2);

        storageFile2 = getStorageFileSample2();
        assertThat(storageFile1).isNotEqualTo(storageFile2);
    }
}
