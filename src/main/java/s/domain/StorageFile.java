package s.domain;

import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A StorageFile.
 */
@Table("storage_file")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class StorageFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Column("name")
    private String name;

    @Column("size")
    private Integer size;

    @Column("mime_type")
    private String mimeType;

    @Column("path")
    private String path;

    @Column("created_by")
    private String createdBy;

    @Column("created_date")
    private Instant createdDate;

    @org.springframework.data.annotation.Transient
    private User user;

    @Column("user_id")
    private Long userId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public StorageFile id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public StorageFile name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSize() {
        return this.size;
    }

    public StorageFile size(Integer size) {
        this.setSize(size);
        return this;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public StorageFile mimeType(String mimeType) {
        this.setMimeType(mimeType);
        return this;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getPath() {
        return this.path;
    }

    public StorageFile path(String path) {
        this.setPath(path);
        return this;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public StorageFile createdBy(String createdBy) {
        this.setCreatedBy(createdBy);
        return this;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public StorageFile createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
        this.userId = user != null ? user.getId() : null;
    }

    public StorageFile user(User user) {
        this.setUser(user);
        return this;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long user) {
        this.userId = user;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StorageFile)) {
            return false;
        }
        return getId() != null && getId().equals(((StorageFile) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "StorageFile{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", size=" + getSize() +
            ", mimeType='" + getMimeType() + "'" +
            ", path='" + getPath() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            "}";
    }
}
