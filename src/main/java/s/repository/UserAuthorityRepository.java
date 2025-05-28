package s.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import s.domain.Authority;

/**
 * Spring Data R2DBC repository for the Authority entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserAuthorityRepository extends R2dbcRepository<Authority, String> {

    @Modifying
    @Query("""
            INSERT INTO jhi_user_authority (user_id, authority_name)
            VALUES (:id, 'ROLE_ACTIVATED')
            """)
    Mono<Void> grantActivatedAuthority(@Param("id") long id);
}
