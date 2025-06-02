package s.service;

import com.google.common.net.HttpHeaders;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.samskivert.mustache.Mustache;
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

@Service
public class ExportStorageInfoPdf extends ExportStorageInfo {

    private static Logger LOG = LoggerFactory.getLogger(ExportStorageInfoPdf.class);

    public ExportStorageInfoPdf(UserService userService, StorageFileRepository storageFileRepository, Mustache.Compiler mustacheCompiler) {
        super(userService, storageFileRepository, mustacheCompiler);
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

    public Mono<ResponseEntity<Resource>> exportPdf() {
        LOG.debug("SERVICE request export StorageInfo in pdf for current user");
        return exportMustacheTemplatePdf()
            .map(bytes -> new ByteArrayResource(bytes))
            .map(resource -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"report.pdf\"")
                .contentType(MediaType.parseMediaType("application/pdf"))
                .body(resource)
            );
    }
}
