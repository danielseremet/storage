package s.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import s.domain.StorageFile;
import s.domain.User;
import s.repository.StorageFileRepository;
import s.repository.UserRepository;
import s.repository.UserReservationsRepository;
import s.service.dto.FileDTO;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Objects;

@Service
public class StorageFileService {
    private static final Logger log = LoggerFactory.getLogger(StorageFileService.class);
    private final StorageFileRepository storageFileRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserReservationsRepository userReservationsRepository;

    public StorageFileService(StorageFileRepository storageFileRepository, UserRepository userRepository, UserService userService, UserReservationsRepository userReservationsRepository) {
        this.storageFileRepository = storageFileRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.userReservationsRepository = userReservationsRepository;
    }

    public Mono<Void> deleteStorageFileById(Long id) {
        return storageFileRepository.deleteById(id);
    }

    public Mono<String> getFileName(Long storageFileId) {
        return storageFileRepository.findById(storageFileId)
            .map(StorageFile::getName)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("file name not found")));
    }

     public Mono<FileDTO> getFileInfo(Mono<FilePart> file) {
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
                    .doOnNext(storageFileIsInteresting -> log.info("Storage file that was saved: {}", storageFileIsInteresting));
            })
            .then();
    }


}
