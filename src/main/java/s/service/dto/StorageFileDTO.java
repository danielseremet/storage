package s.service.dto;

import java.time.Instant;

public record StorageFileDTO(FileDTO fileDTO, String path, String createdBy, Instant createdDate,Long UserId) {
    public int size() {
        return (int)fileDTO.size();
    }

    public String name() {
        return fileDTO.name();
    }

    public String context() {
        return fileDTO().content();
    }
}
