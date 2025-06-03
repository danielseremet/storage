package s.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface ExportStorageFiles {
    Mono<ResponseEntity<Resource>> export();

}
