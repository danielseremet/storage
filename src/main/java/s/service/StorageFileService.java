package s.service;


import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import s.domain.StorageFile;
import s.domain.User;
import s.repository.StorageFileRepository;
import s.repository.UserReservationsRepository;
import s.service.dto.FileDTO;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Service
public class StorageFileService {
    private static final Logger log = LoggerFactory.getLogger(StorageFileService.class);
    private final StorageFileRepository storageFileRepository;
    private final UserService userService;
    private final UserReservationsRepository userReservationsRepository;
    private final MailService mailService;

    private static final Logger LOG = LoggerFactory.getLogger(StorageFileService.class);
    private final UserReservationsService userReservationsService;
    private final Mustache.Compiler mustacheCompiler;


    public StorageFileService(StorageFileRepository storageFileRepository, UserService userService, UserReservationsRepository userReservationsRepository, MailService mailService, UserReservationsService userReservationsService, Mustache.Compiler mustacheCompiler ) {
        this.storageFileRepository = storageFileRepository;
        this.userService = userService;
        this.userReservationsRepository = userReservationsRepository;
        this.mailService = mailService;
        this.userReservationsService = userReservationsService;
        this.mustacheCompiler = mustacheCompiler;

    }

    public Mono<Void> deleteStorageFileById(Long id) {
        return storageFileRepository.deleteById(id);
    }

    public Mono<String> getFileName(Long storageFileId) {
        LOG.debug("SERVICE request get file name {}", storageFileId);
        return storageFileRepository.findById(storageFileId)
            .map(StorageFile::getName)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("file name not found")));
    }

     public Mono<FileDTO> getFileInfo(Mono<FilePart> file) {
        LOG.debug("SERVICE request get file info");
        return file.flatMap(filePart -> {
            String fileName = filePart.filename();
            String contextType = Objects.requireNonNull(filePart.headers().getContentType()).toString();
            return DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    Long initialSize = (long) bytes.length;
                    double size = bytes.length / (1024.0 * 1024.0);
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                    return new FileDTO(size, fileName, contextType, inputStream, initialSize);
                });
        });
    }

    public Mono<Boolean> isEnoughStorage(FileDTO file) {
        LOG.debug("SERVICE request isEnough storage");
        return  userService.getCurrentUser()
                .map(User::getId)
                .flatMap(userReservationsRepository::findByUserId)
                .map(reservation -> {
                    int usedSize = reservation.getUsedSize();
                    int totalSize = reservation.getTotalSize();
                    return usedSize + file.size() <= totalSize ? Boolean.TRUE : Boolean.FALSE;
                });

    }


    public Mono<Void> saveUploadedFileInfo(FileDTO file, String bucket) {
        LOG.debug("SERVICE request save uploaded file info {}", file);
        StorageFile storageFile = new StorageFile();
        storageFile.setName(file.name());
        storageFile.setSize((int) file.size());
        storageFile.setMimeType(file.content());
        storageFile.setPath("/" + bucket + "/" + file.name());
        storageFile.setCreatedDate(Instant.now());

        return userService.getCurrentUser()
            .flatMap(user -> {
                storageFile.setCreatedBy(user.getLogin());
                storageFile.setUserId(user.getId());
                log.info("save uploaded file info {}", storageFile.getUserId());
                return storageFileRepository.save(storageFile)
                    .flatMap(ignored ->  userReservationsService.updateUsedSize(file.size()));
            })
            .then();
    }


    public Mono<Void> sendWarningEmailIfNeeded() {
        LOG.debug("SERVICE request check id Warning Email is needed");
        return userService.getCurrentUser()
            .flatMap(user ->
                userReservationsRepository.findById(user.getId())
                    .flatMap(reservation -> {
                        int usedSize = reservation.getUsedSize();
                        int totalSize = reservation.getTotalSize();
                        if (totalSize - usedSize <= totalSize * 0.2) {
                            LOG.debug("REQUEST sending warning email");
                            return Mono.fromRunnable(()-> mailService.sendStorageDepletingMail(user, totalSize - usedSize, totalSize));
                        } else {
                            return Mono.empty();
                        }
                    })
            );
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

                        Template tmpl = mustacheCompiler.compile(new InputStreamReader(getClass().getResourceAsStream("/templates/exportStorageFileTemplate.mustache")));
                        String renderHtml = tmpl.execute(context);
                        return Mono.just(renderHtml);
                    })
            );
    }


    public Mono<String> exportCsvMustacheTemplate() {
        LOG.debug("SERVICE request rendered mustache csv template for current user");
        return userService.getCurrentUser()
                .flatMap(user->
                    storageFileRepository.findByUser(user.getId())
                    .collectList()
                    .flatMap(storageFiles -> {
                        Map<String, Object> context = new HashMap<>();
                        context.put("storageFiles", storageFiles);

                        Template tmpl = mustacheCompiler.compile(new InputStreamReader(getClass().getResourceAsStream("/templates/mustache/exportStorageFileToCsvTemplate.mustache")));
                        String renderHtml = tmpl.execute(context);
                        return Mono.just(renderHtml);
                    })
                );
    }



}
