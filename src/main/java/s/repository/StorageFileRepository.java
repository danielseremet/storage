package s.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import s.domain.StorageFile;
import s.service.dto.StorageFileDTO;

/**
 * Spring Data R2DBC repository for the StorageFile entity.
 */
@SuppressWarnings("unused")
@Repository
public interface StorageFileRepository extends ReactiveCrudRepository<StorageFile, Long>, StorageFileRepositoryInternal {
    @Query("SELECT * FROM storage_file entity WHERE entity.user_id = :id")
    Flux<StorageFile> findByUser(Long id);

    @Query("SELECT * FROM storage_file entity WHERE entity.user_id IS NULL")
    Flux<StorageFile> findAllWhereUserIsNull();

    @Override
    <S extends StorageFile> Mono<S> save(S entity);

    @Override
    Flux<StorageFile> findAll();

    @Override
    Mono<StorageFile> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);



}

interface StorageFileRepositoryInternal {
    <S extends StorageFile> Mono<S> save(S entity);

    Flux<StorageFile> findAllBy(Pageable pageable);

    Flux<StorageFile> findAll();

    Mono<StorageFile> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<StorageFile> findAllBy(Pageable pageable, Criteria criteria);
}
