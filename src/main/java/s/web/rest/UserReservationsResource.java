package s.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import s.domain.UserReservations;
import s.repository.UserRepository;
import s.repository.UserReservationsRepository;
import s.service.MailService;
import s.service.UserReservationsService;
import s.web.rest.errors.BadRequestAlertException;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;


/**
 * REST controller for managing {@link s.domain.UserReservations}.
 */

@RestController
@PreAuthorize("hasAuthority('ROLE_ACTIVATED')")
@RequestMapping("/api/user-reservations")
@Transactional
public class UserReservationsResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserReservationsResource.class);

    private static final String ENTITY_NAME = "userReservations";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UserReservationsRepository userReservationsRepository;
    private final MailService mailService;
    private final UserRepository userRepository;
    private final UserReservationsService userReservationsService;

    public UserReservationsResource(UserReservationsRepository userReservationsRepository, MailService mailService, UserRepository userRepository, UserReservationsService userReservationsService ) {
        this.userReservationsRepository = userReservationsRepository;
        this.mailService = mailService;
        this.userRepository = userRepository;
        this.userReservationsService = userReservationsService;
    }


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/get_active_requests")
    public Flux<UserReservations> getUsersReservationsRequests() {
        return userReservationsService.getAwaitingApprovalReservations();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/approve_reservation/{id}")
    public Mono<Void> approveUserReservations(@PathVariable(value = "id") final Long id) {
        LOG.debug("REST request to approve UserReservations : {}", id);
        return userReservationsService.approveUserReservations(id);
    }

    /**
     * {@code POST  /user-reservations} : Create a new userReservations.
     *
     * @param userReservations the userReservations to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new userReservations, or with status {@code 400 (Bad Request)} if the userReservations has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<UserReservations>> createUserReservations(@RequestBody UserReservations userReservations)
        throws URISyntaxException {
        LOG.debug("REST request to save UserReservations : {}", userReservations);
        if (userReservations.getId() != null) {
            throw new BadRequestAlertException("A new userReservations cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return userReservationsRepository
            .save(userReservations)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/user-reservations/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }


    /**
     * {@code PUT  /user-reservations/:id} : Updates an existing userReservations.
     *
     * @param id               the id of the userReservations to save.
     * @param userReservations the userReservations to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated userReservations,
     * or with status {@code 400 (Bad Request)} if the userReservations is not valid,
     * or with status {@code 500 (Internal Server Error)} if the userReservations couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserReservations>> updateUserReservations(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody UserReservations userReservations
    ) throws URISyntaxException {
        LOG.debug("REST request to update UserReservations : {}, {}", id, userReservations);
        if (userReservations.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, userReservations.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return userReservationsRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return userReservationsRepository
                    .save(userReservations)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /user-reservations/:id} : Partial updates given fields of an existing userReservations, field will ignore if it is null
     *
     * @param id               the id of the userReservations to save.
     * @param userReservations the userReservations to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated userReservations,
     * or with status {@code 400 (Bad Request)} if the userReservations is not valid,
     * or with status {@code 404 (Not Found)} if the userReservations is not found,
     * or with status {@code 500 (Internal Server Error)} if the userReservations couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = {"application/json", "application/merge-patch+json"})
    public Mono<ResponseEntity<UserReservations>> partialUpdateUserReservations(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody UserReservations userReservations
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update UserReservations partially : {}, {}", id, userReservations);
        if (userReservations.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, userReservations.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return userReservationsRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<UserReservations> result = userReservationsRepository
                    .findById(userReservations.getId())
                    .map(existingUserReservations -> {
                        if (userReservations.getTotalSize() != null) {
                            existingUserReservations.setTotalSize(userReservations.getTotalSize());
                        }
                        if (userReservations.getUsedSize() != null) {
                            existingUserReservations.setUsedSize(userReservations.getUsedSize());
                        }
                        if (userReservations.getActivated() != null) {
                            existingUserReservations.setActivated(userReservations.getActivated());
                        }
                        if (userReservations.getCreatedBy() != null) {
                            existingUserReservations.setCreatedBy(userReservations.getCreatedBy());
                        }
                        if (userReservations.getCreatedDate() != null) {
                            existingUserReservations.setCreatedDate(userReservations.getCreatedDate());
                        }

                        return existingUserReservations;
                    })
                    .flatMap(userReservationsRepository::save);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /user-reservations} : get all the userReservations.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of userReservations in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<UserReservations>> getAllUserReservations() {
        LOG.debug("REST request to get all UserReservations");
        return userReservationsRepository.findAll().collectList();
    }

    /**
     * {@code GET  /user-reservations} : get all the userReservations as a stream.
     *
     * @return the {@link Flux} of userReservations.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<UserReservations> getAllUserReservationsAsStream() {
        LOG.debug("REST request to get all UserReservations as a stream");
        return userReservationsRepository.findAll();
    }

    /**
     * {@code GET  /user-reservations/:id} : get the "id" userReservations.
     *
     * @param id the id of the userReservations to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the userReservations, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserReservations>> getUserReservations(@PathVariable("id") Long id) {
        LOG.debug("REST request to get UserReservations : {}", id);
        Mono<UserReservations> userReservations = userReservationsRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(userReservations);
    }

    /**
     * {@code DELETE  /user-reservations/:id} : delete the "id" userReservations.
     *
     * @param id the id of the userReservations to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUserReservations(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete UserReservations : {}", id);
        return userReservationsRepository
            .deleteById(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}
