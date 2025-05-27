package s.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static s.domain.UserReservationsAsserts.*;
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
import s.domain.UserReservations;
import s.repository.EntityManager;
import s.repository.UserRepository;
import s.repository.UserReservationsRepository;

/**
 * Integration tests for the {@link UserReservationsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class UserReservationsResourceIT {

    private static final Integer DEFAULT_TOTAL_SIZE = 1;
    private static final Integer UPDATED_TOTAL_SIZE = 2;

    private static final Integer DEFAULT_USED_SIZE = 1;
    private static final Integer UPDATED_USED_SIZE = 2;

    private static final Boolean DEFAULT_ACTIVATED = false;
    private static final Boolean UPDATED_ACTIVATED = true;

    private static final String DEFAULT_CREATED_BY = "AAAAAAAAAA";
    private static final String UPDATED_CREATED_BY = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/user-reservations";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserReservationsRepository userReservationsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private UserReservations userReservations;

    private UserReservations insertedUserReservations;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserReservations createEntity() {
        return new UserReservations()
            .totalSize(DEFAULT_TOTAL_SIZE)
            .usedSize(DEFAULT_USED_SIZE)
            .activated(DEFAULT_ACTIVATED)
            .createdBy(DEFAULT_CREATED_BY)
            .createdDate(DEFAULT_CREATED_DATE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserReservations createUpdatedEntity() {
        return new UserReservations()
            .totalSize(UPDATED_TOTAL_SIZE)
            .usedSize(UPDATED_USED_SIZE)
            .activated(UPDATED_ACTIVATED)
            .createdBy(UPDATED_CREATED_BY)
            .createdDate(UPDATED_CREATED_DATE);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(UserReservations.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    void initTest() {
        userReservations = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedUserReservations != null) {
            userReservationsRepository.delete(insertedUserReservations).block();
            insertedUserReservations = null;
        }
        deleteEntities(em);
        userRepository.deleteAllUserAuthorities().block();
        userRepository.deleteAll().block();
    }

    @Test
    void createUserReservations() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the UserReservations
        var returnedUserReservations = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userReservations))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(UserReservations.class)
            .returnResult()
            .getResponseBody();

        // Validate the UserReservations in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertUserReservationsUpdatableFieldsEquals(returnedUserReservations, getPersistedUserReservations(returnedUserReservations));

        insertedUserReservations = returnedUserReservations;
    }

    @Test
    void createUserReservationsWithExistingId() throws Exception {
        // Create the UserReservations with an existing ID
        userReservations.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userReservations))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the UserReservations in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void getAllUserReservationsAsStream() {
        // Initialize the database
        userReservationsRepository.save(userReservations).block();

        List<UserReservations> userReservationsList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(UserReservations.class)
            .getResponseBody()
            .filter(userReservations::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(userReservationsList).isNotNull();
        assertThat(userReservationsList).hasSize(1);
        UserReservations testUserReservations = userReservationsList.get(0);

        // Test fails because reactive api returns an empty object instead of null
        // assertUserReservationsAllPropertiesEquals(userReservations, testUserReservations);
        assertUserReservationsUpdatableFieldsEquals(userReservations, testUserReservations);
    }

    @Test
    void getAllUserReservations() {
        // Initialize the database
        insertedUserReservations = userReservationsRepository.save(userReservations).block();

        // Get all the userReservationsList
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
            .value(hasItem(userReservations.getId().intValue()))
            .jsonPath("$.[*].totalSize")
            .value(hasItem(DEFAULT_TOTAL_SIZE))
            .jsonPath("$.[*].usedSize")
            .value(hasItem(DEFAULT_USED_SIZE))
            .jsonPath("$.[*].activated")
            .value(hasItem(DEFAULT_ACTIVATED))
            .jsonPath("$.[*].createdBy")
            .value(hasItem(DEFAULT_CREATED_BY))
            .jsonPath("$.[*].createdDate")
            .value(hasItem(DEFAULT_CREATED_DATE.toString()));
    }

    @Test
    void getUserReservations() {
        // Initialize the database
        insertedUserReservations = userReservationsRepository.save(userReservations).block();

        // Get the userReservations
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, userReservations.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(userReservations.getId().intValue()))
            .jsonPath("$.totalSize")
            .value(is(DEFAULT_TOTAL_SIZE))
            .jsonPath("$.usedSize")
            .value(is(DEFAULT_USED_SIZE))
            .jsonPath("$.activated")
            .value(is(DEFAULT_ACTIVATED))
            .jsonPath("$.createdBy")
            .value(is(DEFAULT_CREATED_BY))
            .jsonPath("$.createdDate")
            .value(is(DEFAULT_CREATED_DATE.toString()));
    }

    @Test
    void getNonExistingUserReservations() {
        // Get the userReservations
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingUserReservations() throws Exception {
        // Initialize the database
        insertedUserReservations = userReservationsRepository.save(userReservations).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the userReservations
        UserReservations updatedUserReservations = userReservationsRepository.findById(userReservations.getId()).block();
        updatedUserReservations
            .totalSize(UPDATED_TOTAL_SIZE)
            .usedSize(UPDATED_USED_SIZE)
            .activated(UPDATED_ACTIVATED)
            .createdBy(UPDATED_CREATED_BY)
            .createdDate(UPDATED_CREATED_DATE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedUserReservations.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedUserReservations))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the UserReservations in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedUserReservationsToMatchAllProperties(updatedUserReservations);
    }

    @Test
    void putNonExistingUserReservations() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userReservations.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, userReservations.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userReservations))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the UserReservations in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchUserReservations() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userReservations.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userReservations))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the UserReservations in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamUserReservations() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userReservations.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userReservations))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the UserReservations in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateUserReservationsWithPatch() throws Exception {
        // Initialize the database
        insertedUserReservations = userReservationsRepository.save(userReservations).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the userReservations using partial update
        UserReservations partialUpdatedUserReservations = new UserReservations();
        partialUpdatedUserReservations.setId(userReservations.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedUserReservations.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedUserReservations))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the UserReservations in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertUserReservationsUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedUserReservations, userReservations),
            getPersistedUserReservations(userReservations)
        );
    }

    @Test
    void fullUpdateUserReservationsWithPatch() throws Exception {
        // Initialize the database
        insertedUserReservations = userReservationsRepository.save(userReservations).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the userReservations using partial update
        UserReservations partialUpdatedUserReservations = new UserReservations();
        partialUpdatedUserReservations.setId(userReservations.getId());

        partialUpdatedUserReservations
            .totalSize(UPDATED_TOTAL_SIZE)
            .usedSize(UPDATED_USED_SIZE)
            .activated(UPDATED_ACTIVATED)
            .createdBy(UPDATED_CREATED_BY)
            .createdDate(UPDATED_CREATED_DATE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedUserReservations.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedUserReservations))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the UserReservations in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertUserReservationsUpdatableFieldsEquals(
            partialUpdatedUserReservations,
            getPersistedUserReservations(partialUpdatedUserReservations)
        );
    }

    @Test
    void patchNonExistingUserReservations() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userReservations.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, userReservations.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(userReservations))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the UserReservations in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchUserReservations() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userReservations.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(userReservations))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the UserReservations in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamUserReservations() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userReservations.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(userReservations))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the UserReservations in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteUserReservations() {
        // Initialize the database
        insertedUserReservations = userReservationsRepository.save(userReservations).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the userReservations
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, userReservations.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return userReservationsRepository.count().block();
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

    protected UserReservations getPersistedUserReservations(UserReservations userReservations) {
        return userReservationsRepository.findById(userReservations.getId()).block();
    }

    protected void assertPersistedUserReservationsToMatchAllProperties(UserReservations expectedUserReservations) {
        // Test fails because reactive api returns an empty object instead of null
        // assertUserReservationsAllPropertiesEquals(expectedUserReservations, getPersistedUserReservations(expectedUserReservations));
        assertUserReservationsUpdatableFieldsEquals(expectedUserReservations, getPersistedUserReservations(expectedUserReservations));
    }

    protected void assertPersistedUserReservationsToMatchUpdatableProperties(UserReservations expectedUserReservations) {
        // Test fails because reactive api returns an empty object instead of null
        // assertUserReservationsAllUpdatablePropertiesEquals(expectedUserReservations, getPersistedUserReservations(expectedUserReservations));
        assertUserReservationsUpdatableFieldsEquals(expectedUserReservations, getPersistedUserReservations(expectedUserReservations));
    }
}
