package s.config;


import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MinioClientConfig {

    @Bean
    public MinioClient minioClient(@Qualifier("minioConfig") MinioConfig config) {
        return MinioClient.builder()
            .endpoint(config.getUrl())
            .credentials(config.getAccessKey(), config.getSecretKey())
            .build();
    }
}
