package s.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import s.domain.UserReservations;

/**
 * Spring Data R2DBC repository for the UserReservations entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserReservationsRepository extends ReactiveCrudRepository<UserReservations, Long>, UserReservationsRepositoryInternal {
    @Query("SELECT * FROM user_reservations entity WHERE entity.user_id = :id")
    Flux<UserReservations> findByUser(Long id);

    @Query("SELECT * FROM user_reservations entity WHERE entity.user_id IS NULL")
    Flux<UserReservations> findAllWhereUserIsNull();

    @Override
    <S extends UserReservations> Mono<S> save(S entity);

    @Override
    Flux<UserReservations> findAll();

    @Override
    Mono<UserReservations> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);

    @Modifying
    @Query("""
            INSERT INTO user_reservations(total_size, used_size, activated, created_by, created_date, user_id)
            SELECT 200 , 0 , false ,:name,CURRENT_TIMESTAMP ,id
            FROM jhi_user
            where login = :name

            """)
     Mono<Void> saveUserReservation(@Param("name") String name);



    @Query("""
            Select * FROM user_reservations
            WHERE activated = false
            """)
    Flux<UserReservations> getWaitingForApproval();

    @Modifying
    @Query("""
            UPDATE user_reservations
            SET activated = true
            WHERE user_id = :user_id
            """)
    Mono<Void> approveUsersReservation(@Param("user_id") long user_id);

}

interface UserReservationsRepositoryInternal {
    <S extends UserReservations> Mono<S> save(S entity);

    Flux<UserReservations> findAllBy(Pageable pageable);

    Flux<UserReservations> findAll();

    Mono<UserReservations> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<UserReservations> findAllBy(Pageable pageable, Criteria criteria);
}
