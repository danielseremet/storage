package s.repository;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import s.domain.User;
import s.domain.UserReservations;
import s.repository.rowmapper.UserReservationsRowMapper;
import s.repository.rowmapper.UserRowMapper;

/**
 * Spring Data R2DBC custom repository implementation for the UserReservations entity.
 */
@SuppressWarnings("unused")
class UserReservationsRepositoryInternalImpl
    extends SimpleR2dbcRepository<UserReservations, Long>
    implements UserReservationsRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final UserRowMapper userMapper;
    private final UserReservationsRowMapper userreservationsMapper;

    private final UserRepository userRepository;

    private static final Table entityTable = Table.aliased("user_reservations", EntityManager.ENTITY_ALIAS);
    private static final Table userTable = Table.aliased("jhi_user", "e_user");

    public UserReservationsRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        UserRowMapper userMapper,
        UserReservationsRowMapper userreservationsMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter, UserRepository userRepository
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(UserReservations.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.userMapper = userMapper;
        this.userreservationsMapper = userreservationsMapper;
        this.userRepository = userRepository;
    }

    @Override
    public Flux<UserReservations> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<UserReservations> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = UserReservationsSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(UserSqlHelper.getColumns(userTable, "user"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(userTable)
            .on(Column.create("user_id", entityTable))
            .equals(Column.create("id", userTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, UserReservations.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<UserReservations> findAll() {
        return findAllBy(null);
    }




    @Override
    public Mono<UserReservations> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private UserReservations process(Row row, RowMetadata metadata) {
        UserReservations entity = userreservationsMapper.apply(row, "e");
        entity.setUser(userMapper.apply(row, "user"));
        return entity;
    }


    @Override
    public <S extends UserReservations> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
