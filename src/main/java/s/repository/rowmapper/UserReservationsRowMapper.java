package s.repository.rowmapper;

import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;
import s.domain.UserReservations;

/**
 * Converter between {@link Row} to {@link UserReservations}, with proper type conversions.
 */
@Service
public class UserReservationsRowMapper implements BiFunction<Row, String, UserReservations> {

    private final ColumnConverter converter;

    public UserReservationsRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link UserReservations} stored in the database.
     */
    @Override
    public UserReservations apply(Row row, String prefix) {
        UserReservations entity = new UserReservations();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setTotalSize(converter.fromRow(row, prefix + "_total_size", Integer.class));
        entity.setUsedSize(converter.fromRow(row, prefix + "_used_size", Integer.class));
        entity.setActivated(converter.fromRow(row, prefix + "_activated", Boolean.class));
        entity.setCreatedBy(converter.fromRow(row, prefix + "_created_by", String.class));
        entity.setCreatedDate(converter.fromRow(row, prefix + "_created_date", Instant.class));
        entity.setUserId(converter.fromRow(row, prefix + "_user_id", Long.class));
        return entity;
    }
}
