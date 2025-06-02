package s.service;


import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
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
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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



    public StorageFileService(StorageFileRepository storageFileRepository, UserService userService, UserReservationsRepository userReservationsRepository, MailService mailService, UserReservationsService userReservationsService  ) {
        this.storageFileRepository = storageFileRepository;
        this.userService = userService;
        this.userReservationsRepository = userReservationsRepository;
        this.mailService = mailService;
        this.userReservationsService = userReservationsService;
    }

    public Mono<Void> deleteStorageFileById(Long id) {
        return storageFileRepository.findById(id)
            .flatMap(storageFile ->
                userReservationsService.updateUsedSize(-storageFile.getSize())
                    .doOnNext(nothing -> LOG.info("succesfully udated used size for deleated the file size :{}",storageFile.getSize()))
                    .then(storageFileRepository.deleteById(id))
            ).then();
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
        String encryptedName = null;
         try {
             final SecretKey key = (AESutil.generateKey());
             encryptedName = AESutil.encrypt(file.name(), key);
         }catch(Exception e) {
             LOG.debug("An Error has occured while generating the enctription :{}", e);
         }
        StorageFile storageFile = new StorageFile();
        storageFile.setName(file.name());
        storageFile.setSize((int) file.size());
        storageFile.setMimeType(file.content());
        storageFile.setPath("/" + bucket + "/" + encryptedName);
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












}
