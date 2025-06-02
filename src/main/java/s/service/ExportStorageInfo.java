package s.service;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import s.repository.StorageFileRepository;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExportStorageInfo {
    private static Logger LOG = LoggerFactory.getLogger(ExportStorageInfo.class);

    private final UserService userService;
    private final StorageFileRepository storageFileRepository;
    private final Mustache.Compiler mustacheCompiler;

    public ExportStorageInfo(UserService userService, StorageFileRepository storageFileRepository, Mustache.Compiler mustacheCompiler) {
        this.userService = userService;
        this.storageFileRepository = storageFileRepository;
        this.mustacheCompiler = mustacheCompiler;
    }


    public Mono<String> renderedMustacheTemplate() {
        LOG.debug("SERVICE request rendered mustache template for current user");
        return userService.getCurrentUser()
            .flatMap(user->
                storageFileRepository.findByUser(user.getId())
                    .collectList()
                    .flatMap(storageFiles -> {
                        Map<String, Object> context = new HashMap<>();
                        context.put("storageFiles", storageFiles);

                        Template tmpl = mustacheCompiler.compile(new InputStreamReader(getClass().getResourceAsStream("/templates/mustache/exportStorageFileTemplate.mustache")));
                        String renderHtml = tmpl.execute(context);
                        return Mono.just(renderHtml);
                    })
            );
    }
}
