package s.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static s.domain.StorageFileAsserts.*;
import static s.web.rest.TestUtil.createUpdateProxyForBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import s.IntegrationTest;
import s.domain.StorageFile;
import s.repository.EntityManager;
import s.repository.StorageFileRepository;
import s.repository.UserRepository;

/**
 * Integration tests for the {@link StorageFileResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class StorageFileResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_SIZE = 1;
    private static final Integer UPDATED_SIZE = 2;

    private static final String DEFAULT_MIME_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_MIME_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_PATH = "AAAAAAAAAA";
    private static final String UPDATED_PATH = "BBBBBBBBBB";

    private static final String DEFAULT_CREATED_BY = "AAAAAAAAAA";
    private static final String UPDATED_CREATED_BY = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/storage-files";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private StorageFileRepository storageFileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private StorageFile storageFile;

    private StorageFile insertedStorageFile;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static StorageFile createEntity() {
        return new StorageFile()
            .name(DEFAULT_NAME)
            .size(DEFAULT_SIZE)
            .mimeType(DEFAULT_MIME_TYPE)
            .path(DEFAULT_PATH)
            .createdBy(DEFAULT_CREATED_BY)
            .createdDate(DEFAULT_CREATED_DATE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static StorageFile createUpdatedEntity() {
        return new StorageFile()
            .name(UPDATED_NAME)
            .size(UPDATED_SIZE)
            .mimeType(UPDATED_MIME_TYPE)
            .path(UPDATED_PATH)
            .createdBy(UPDATED_CREATED_BY)
            .createdDate(UPDATED_CREATED_DATE);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(StorageFile.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    void initTest() {
        storageFile = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedStorageFile != null) {
            storageFileRepository.delete(insertedStorageFile).block();
            insertedStorageFile = null;
        }
        deleteEntities(em);
        userRepository.deleteAllUserAuthorities().block();
        userRepository.deleteAll().block();
    }

    @Test
    void createStorageFile() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the StorageFile
        var returnedStorageFile = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(storageFile))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(StorageFile.class)
            .returnResult()
            .getResponseBody();

        // Validate the StorageFile in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertStorageFileUpdatableFieldsEquals(returnedStorageFile, getPersistedStorageFile(returnedStorageFile));

        insertedStorageFile = returnedStorageFile;
    }

    @Test
    void createStorageFileWithExistingId() throws Exception {
        // Create the StorageFile with an existing ID
        storageFile.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(storageFile))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the StorageFile in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void getAllStorageFilesAsStream() {
        // Initialize the database
        storageFileRepository.save(storageFile).block();

        List<StorageFile> storageFileList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(StorageFile.class)
            .getResponseBody()
            .filter(storageFile::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(storageFileList).isNotNull();
        assertThat(storageFileList).hasSize(1);
        StorageFile testStorageFile = storageFileList.get(0);

        // Test fails because reactive api returns an empty object instead of null
        // assertStorageFileAllPropertiesEquals(storageFile, testStorageFile);
        assertStorageFileUpdatableFieldsEquals(storageFile, testStorageFile);
    }

    @Test
    void getAllStorageFiles() {
        // Initialize the database
        insertedStorageFile = storageFileRepository.save(storageFile).block();

        // Get all the storageFileList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(storageFile.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].size")
            .value(hasItem(DEFAULT_SIZE))
            .jsonPath("$.[*].mimeType")
            .value(hasItem(DEFAULT_MIME_TYPE))
            .jsonPath("$.[*].path")
            .value(hasItem(DEFAULT_PATH))
            .jsonPath("$.[*].createdBy")
            .value(hasItem(DEFAULT_CREATED_BY))
            .jsonPath("$.[*].createdDate")
            .value(hasItem(DEFAULT_CREATED_DATE.toString()));
    }

    @Test
    void getStorageFile() {
        // Initialize the database
        insertedStorageFile = storageFileRepository.save(storageFile).block();

        // Get the storageFile
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, storageFile.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(storageFile.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.size")
            .value(is(DEFAULT_SIZE))
            .jsonPath("$.mimeType")
            .value(is(DEFAULT_MIME_TYPE))
            .jsonPath("$.path")
            .value(is(DEFAULT_PATH))
            .jsonPath("$.createdBy")
            .value(is(DEFAULT_CREATED_BY))
            .jsonPath("$.createdDate")
            .value(is(DEFAULT_CREATED_DATE.toString()));
    }

    @Test
    void getNonExistingStorageFile() {
        // Get the storageFile
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingStorageFile() throws Exception {
        // Initialize the database
        insertedStorageFile = storageFileRepository.save(storageFile).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the storageFile
        StorageFile updatedStorageFile = storageFileRepository.findById(storageFile.getId()).block();
        updatedStorageFile
            .name(UPDATED_NAME)
            .size(UPDATED_SIZE)
            .mimeType(UPDATED_MIME_TYPE)
            .path(UPDATED_PATH)
            .createdBy(UPDATED_CREATED_BY)
            .createdDate(UPDATED_CREATED_DATE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedStorageFile.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedStorageFile))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the StorageFile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedStorageFileToMatchAllProperties(updatedStorageFile);
    }

    @Test
    void putNonExistingStorageFile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        storageFile.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, storageFile.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(storageFile))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the StorageFile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchStorageFile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        storageFile.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(storageFile))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the StorageFile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamStorageFile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        storageFile.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(storageFile))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the StorageFile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateStorageFileWithPatch() throws Exception {
        // Initialize the database
        insertedStorageFile = storageFileRepository.save(storageFile).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the storageFile using partial update
        StorageFile partialUpdatedStorageFile = new StorageFile();
        partialUpdatedStorageFile.setId(storageFile.getId());

        partialUpdatedStorageFile.size(UPDATED_SIZE).path(UPDATED_PATH).createdDate(UPDATED_CREATED_DATE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedStorageFile.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedStorageFile))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the StorageFile in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertStorageFileUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedStorageFile, storageFile),
            getPersistedStorageFile(storageFile)
        );
    }

    @Test
    void fullUpdateStorageFileWithPatch() throws Exception {
        // Initialize the database
        insertedStorageFile = storageFileRepository.save(storageFile).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the storageFile using partial update
        StorageFile partialUpdatedStorageFile = new StorageFile();
        partialUpdatedStorageFile.setId(storageFile.getId());

        partialUpdatedStorageFile
            .name(UPDATED_NAME)
            .size(UPDATED_SIZE)
            .mimeType(UPDATED_MIME_TYPE)
            .path(UPDATED_PATH)
            .createdBy(UPDATED_CREATED_BY)
            .createdDate(UPDATED_CREATED_DATE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedStorageFile.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedStorageFile))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the StorageFile in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertStorageFileUpdatableFieldsEquals(partialUpdatedStorageFile, getPersistedStorageFile(partialUpdatedStorageFile));
    }

    @Test
    void patchNonExistingStorageFile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        storageFile.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, storageFile.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(storageFile))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the StorageFile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchStorageFile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        storageFile.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(storageFile))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the StorageFile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamStorageFile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        storageFile.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(storageFile))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the StorageFile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteStorageFile() {
        // Initialize the database
        insertedStorageFile = storageFileRepository.save(storageFile).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the storageFile
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, storageFile.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return storageFileRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected StorageFile getPersistedStorageFile(StorageFile storageFile) {
        return storageFileRepository.findById(storageFile.getId()).block();
    }

    protected void assertPersistedStorageFileToMatchAllProperties(StorageFile expectedStorageFile) {
        // Test fails because reactive api returns an empty object instead of null
        // assertStorageFileAllPropertiesEquals(expectedStorageFile, getPersistedStorageFile(expectedStorageFile));
        assertStorageFileUpdatableFieldsEquals(expectedStorageFile, getPersistedStorageFile(expectedStorageFile));
    }

    protected void assertPersistedStorageFileToMatchUpdatableProperties(StorageFile expectedStorageFile) {
        // Test fails because reactive api returns an empty object instead of null
        // assertStorageFileAllUpdatablePropertiesEquals(expectedStorageFile, getPersistedStorageFile(expectedStorageFile));
        assertStorageFileUpdatableFieldsEquals(expectedStorageFile, getPersistedStorageFile(expectedStorageFile));
    }
}
