package s.repository.rowmapper;

import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;
import s.domain.StorageFile;

/**
 * Converter between {@link Row} to {@link StorageFile}, with proper type conversions.
 */
@Service
public class StorageFileRowMapper implements BiFunction<Row, String, StorageFile> {

    private final ColumnConverter converter;

    public StorageFileRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link StorageFile} stored in the database.
     */
    @Override
    public StorageFile apply(Row row, String prefix) {
        StorageFile entity = new StorageFile();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setSize(converter.fromRow(row, prefix + "_size", Integer.class));
        entity.setMimeType(converter.fromRow(row, prefix + "_mime_type", String.class));
        entity.setPath(converter.fromRow(row, prefix + "_path", String.class));
        entity.setCreatedBy(converter.fromRow(row, prefix + "_created_by", String.class));
        entity.setCreatedDate(converter.fromRow(row, prefix + "_created_date", Instant.class));
        entity.setUserId(converter.fromRow(row, prefix + "_user_id", Long.class));
        return entity;
    }
}
