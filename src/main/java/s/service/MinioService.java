package s.service;


import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import io.vavr.control.Try;
import reactor.core.scheduler.Schedulers;
import s.config.ApplicationProperties;

import java.io.InputStream;


@Service
public class MinioService {

    private static final Logger LOG = LoggerFactory.getLogger(MinioService.class);

    private final MinioClient minioClient;
    private final StorageFileService storageFileService;
    private final ApplicationProperties.MinioConfig minioConfig;
    private final String bucket;

    public MinioService(MinioClient minioClient, ApplicationProperties  applicationProperties, StorageFileService storageFileService) {
        this.minioClient = minioClient;
        this.minioConfig = applicationProperties.getMinio();
        this.storageFileService = storageFileService;
        bucket = minioConfig.getBucket();
    }



    public Mono<Void> uploadFile(Mono<FilePart> fileMono) {
        LOG.debug("SERVICE request to upload file : {}", fileMono);
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
                                return storageFileService.saveUploadedFileInfo(file, bucket)
                                    .then(storageFileService.sendWarningEmailIfNeeded());
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
        LOG.debug("SERVICE request to delete file : {}", storageFileId);
        return storageFileService.getFileName(storageFileId)
            .flatMap(name -> Mono.fromRunnable(() ->
                    Try.run(()-> minioClient.removeObject(
                        RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(name)
                            .build()
                    )).onSuccess(nothing-> LOG.debug("File deleted successfully"))
                        .onFailure(ex-> LOG.error("failed to delete from minio", ex))
                    ).subscribeOn(Schedulers.boundedElastic())
            ).then(storageFileService.deleteStorageFileById(storageFileId));
    }

    public Mono<ByteArrayResource> getFile(String fileName) {
        LOG.debug("SERVICE request to download file : {}", fileName);
        return Mono.fromCallable(() -> {
            InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(fileName)
                    .build()
            );
            byte[] bytes = stream.readAllBytes();
            ByteArrayResource resource = new ByteArrayResource(bytes);
            return resource;
        });
    }



}
