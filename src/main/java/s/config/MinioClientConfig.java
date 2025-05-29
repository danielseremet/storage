package s.config;


import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MinioClientConfig {

    @Bean
    public MinioClient minioClient(ApplicationProperties applicationProperties) {
        ApplicationProperties.MinioConfig config = applicationProperties.getMinio();
        return MinioClient.builder()
            .endpoint(config.getUrl())
            .credentials(config.getAccessKey(), config.getSecretKey())
            .build();
    }
}
