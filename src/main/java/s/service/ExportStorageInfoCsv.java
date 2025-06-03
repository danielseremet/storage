package s.service;


import com.google.common.net.HttpHeaders;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import s.repository.StorageFileRepository;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExportStorageInfoCsv implements ExportStorageFiles {
    private static Logger LOG = LoggerFactory.getLogger(ExportStorageInfoCsv.class);

    private final StorageFileRepository storageFileRepository;
    private final UserService userService;
    private final Mustache.Compiler compiler;

    public ExportStorageInfoCsv(UserService userService, StorageFileRepository storageFileRepository,
                                Mustache.Compiler mustacheCompiler, StorageFileRepository storageFileRepository1,
                                UserService userService1, Mustache.Compiler compiler) {
        this.storageFileRepository = storageFileRepository1;
        this.userService = userService;
        this.compiler = compiler;
    }

    public Mono<byte[]> exportMustacheTemplateCsv() {
        LOG.debug("SERVICE request rendered mustache csv template for current user");
        return userService.getCurrentUser()
                .flatMap(user->
                    storageFileRepository.findByUser(user.getId())
                        .collectList()
                        .flatMap(storageFiles -> {
                            Map<String, Object> context = new HashMap<>();
                            context.put("storageFiles", storageFiles);

                            Template tmpl = compiler.compile(new InputStreamReader(
                                getClass().getResourceAsStream(
                                    "/templates/mustache/exportStorageFileToCsvTemplate.mustache")));
                            String renderHtml = tmpl.execute(context);
                            return Mono.just(renderHtml.getBytes());
                        })
                );
        }

    public Mono<ResponseEntity<Resource>> export() {
        LOG.debug("SERVICE request export StorageInfo in csv for current user");
        return exportMustacheTemplateCsv()
            .map(bytes -> new ByteArrayResource(bytes))
            .map(resource -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource)
            );
    }
}
