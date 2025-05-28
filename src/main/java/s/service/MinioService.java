package s.service;


import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import io.vavr.control.Try;
import reactor.core.scheduler.Schedulers;


@Service
public class MinioService {

    private static final Logger LOG = LoggerFactory.getLogger(MinioService.class);

    private final MinioClient minioClient;
    private final String bucket;
    private final StorageFileService storageFileService;

    public MinioService(MinioClient minioClient, @Value("${minio.bucket}") String bucket, StorageFileService storageFileService) {
        this.minioClient = minioClient;
        this.bucket = bucket;
        this.storageFileService = storageFileService;
    }


    public Mono<Void> uploadFile(Mono<FilePart> fileMono) {
        LOG.debug("uploadingFile");
        return storageFileService.getFileInfo(fileMono)
            .flatMap(file ->
                storageFileService.isEnoughStorage(file)
                    .flatMap(bool -> {
                        if (bool) {
                            try {
                                String filename = file.name();

                                minioClient.putObject(
                                    PutObjectArgs.builder()
                                        .bucket(bucket)
                                        .object(filename)
                                        .stream(file.inputStream(), file.sizeInBytes(), -1)
                                        .contentType(file.content())
                                        .build()
                                );
                                return storageFileService.saveUploadedFileInfo(file, bucket);
                            } catch (Exception e) {
                                LOG.debug(e.getMessage());
                                return Mono.error(e);
                            }
                        } else {
                            return Mono.error(new IllegalStateException("Not enough storage space"));
                        }

                    }).then()
            );


    }

    public Mono<Void> deleteFile(Long storageFileId) {
        LOG.debug("deleteFile");
        return storageFileService.getFileName(storageFileId)
            .flatMap(name -> Mono.fromRunnable(() ->
                    Try.run(()-> minioClient.removeObject(
                        RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(name)
                            .build()
                    )).onFailure(ex-> LOG.error("failed to delete from minio", ex))
                    ).subscribeOn(Schedulers.boundedElastic())
            ).then(storageFileService.deleteStorageFileById(storageFileId));
    }


}
