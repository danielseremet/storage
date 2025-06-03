package s.service;

import com.google.common.net.HttpHeaders;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class ExportStorageInfoPdf implements ExportStorageFiles {

    private static Logger LOG = LoggerFactory.getLogger(ExportStorageInfoPdf.class);

    private final UserService userService;
    private final StorageFileRepository storageFileRepository;
    private final Mustache.Compiler mustacheCompiler;

    public ExportStorageInfoPdf(UserService userService, StorageFileRepository storageFileRepository,
                                Mustache.Compiler mustacheCompiler, UserService userService1,
                                StorageFileRepository storageFileRepository1, Mustache.Compiler mustacheCompiler1) {
        this.userService = userService1;
        this.storageFileRepository = storageFileRepository1;
        this.mustacheCompiler = mustacheCompiler1;
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

                        Template tmpl = mustacheCompiler.compile(new InputStreamReader(
                            Objects.requireNonNull(getClass().getResourceAsStream(
                                "/templates/mustache/exportStorageFileTemplate.mustache"))));
                        String renderHtml = tmpl.execute(context);
                        return Mono.just(renderHtml);
                    })
            );
    }

    public Mono<byte[]> exportMustacheTemplatePdf() {
        LOG.debug("SERVICE request rendered mustache template pdf for current user");
        return renderedMustacheTemplate()
            .map(html -> {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.withHtmlContent(html,null);
                builder.toStream(outputStream);
                if(html == null || html.isEmpty()) {
                    LOG.debug("template pdf is empty");
                }
                try {
                    builder.run();
                } catch (IOException e) {
                    LOG.debug("IO exception while rendering html pdf", e);
                }
                return outputStream.toByteArray();
            });
    }

    public Mono<ResponseEntity<Resource>> export() {
        LOG.debug("SERVICE request export StorageInfo in pdf for current user");
        return exportMustacheTemplatePdf()
            .map(ByteArrayResource::new)
            .map(resource -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"report.pdf\"")
                .contentType(MediaType.parseMediaType("application/pdf"))
                .body(resource)
            );
    }
}
