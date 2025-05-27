package s.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
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
import s.domain.StorageFile;
import s.repository.StorageFileRepository;
import s.web.rest.errors.BadRequestAlertException;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link s.domain.StorageFile}.
 */

@RestController
@RequestMapping("/api/storage-files")
@Transactional
public class StorageFileResource {

    private static final Logger LOG = LoggerFactory.getLogger(StorageFileResource.class);

    private static final String ENTITY_NAME = "storageFile";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final StorageFileRepository storageFileRepository;

    public StorageFileResource(StorageFileRepository storageFileRepository) {
        this.storageFileRepository = storageFileRepository;
    }

    /**
     * {@code POST  /storage-files} : Create a new storageFile.
     *
     * @param storageFile the storageFile to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new storageFile, or with status {@code 400 (Bad Request)} if the storageFile has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<StorageFile>> createStorageFile(@RequestBody StorageFile storageFile) throws URISyntaxException {
        LOG.debug("REST request to save StorageFile : {}", storageFile);
        if (storageFile.getId() != null) {
            throw new BadRequestAlertException("A new storageFile cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return storageFileRepository
            .save(storageFile)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/storage-files/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /storage-files/:id} : Updates an existing storageFile.
     *
     * @param id the id of the storageFile to save.
     * @param storageFile the storageFile to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated storageFile,
     * or with status {@code 400 (Bad Request)} if the storageFile is not valid,
     * or with status {@code 500 (Internal Server Error)} if the storageFile couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<StorageFile>> updateStorageFile(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody StorageFile storageFile
    ) throws URISyntaxException {
        LOG.debug("REST request to update StorageFile : {}, {}", id, storageFile);
        if (storageFile.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, storageFile.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return storageFileRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return storageFileRepository
                    .save(storageFile)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /storage-files/:id} : Partial updates given fields of an existing storageFile, field will ignore if it is null
     *
     * @param id the id of the storageFile to save.
     * @param storageFile the storageFile to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated storageFile,
     * or with status {@code 400 (Bad Request)} if the storageFile is not valid,
     * or with status {@code 404 (Not Found)} if the storageFile is not found,
     * or with status {@code 500 (Internal Server Error)} if the storageFile couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<StorageFile>> partialUpdateStorageFile(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody StorageFile storageFile
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update StorageFile partially : {}, {}", id, storageFile);
        if (storageFile.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, storageFile.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return storageFileRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<StorageFile> result = storageFileRepository
                    .findById(storageFile.getId())
                    .map(existingStorageFile -> {
                        if (storageFile.getName() != null) {
                            existingStorageFile.setName(storageFile.getName());
                        }
                        if (storageFile.getSize() != null) {
                            existingStorageFile.setSize(storageFile.getSize());
                        }
                        if (storageFile.getMimeType() != null) {
                            existingStorageFile.setMimeType(storageFile.getMimeType());
                        }
                        if (storageFile.getPath() != null) {
                            existingStorageFile.setPath(storageFile.getPath());
                        }
                        if (storageFile.getCreatedBy() != null) {
                            existingStorageFile.setCreatedBy(storageFile.getCreatedBy());
                        }
                        if (storageFile.getCreatedDate() != null) {
                            existingStorageFile.setCreatedDate(storageFile.getCreatedDate());
                        }

                        return existingStorageFile;
                    })
                    .flatMap(storageFileRepository::save);

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
     * {@code GET  /storage-files} : get all the storageFiles.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of storageFiles in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<StorageFile>> getAllStorageFiles() {
        LOG.debug("REST request to get all StorageFiles");
        return storageFileRepository.findAll().collectList();
    }

    /**
     * {@code GET  /storage-files} : get all the storageFiles as a stream.
     * @return the {@link Flux} of storageFiles.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<StorageFile> getAllStorageFilesAsStream() {
        LOG.debug("REST request to get all StorageFiles as a stream");
        return storageFileRepository.findAll();
    }

    /**
     * {@code GET  /storage-files/:id} : get the "id" storageFile.
     *
     * @param id the id of the storageFile to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the storageFile, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<StorageFile>> getStorageFile(@PathVariable("id") Long id) {
        LOG.debug("REST request to get StorageFile : {}", id);
        Mono<StorageFile> storageFile = storageFileRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(storageFile);
    }

    /**
     * {@code DELETE  /storage-files/:id} : delete the "id" storageFile.
     *
     * @param id the id of the storageFile to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteStorageFile(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete StorageFile : {}", id);
        return storageFileRepository
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
